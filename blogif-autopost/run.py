"""
Main orchestrator: pick a topic -> generate content -> fetch image -> post to WP.

Usage:
    python run.py                      # post 1 draft from topics.txt
    python run.py --count 3            # post 3 drafts
    python run.py --topic "Custom..."  # post a single custom topic
    python run.py --status publish     # publish live (DANGEROUS for AdSense - use draft)
    python run.py --test               # only test WP + AI connections
"""
import argparse
import json
import os
import sys
from datetime import datetime
from pathlib import Path

from dotenv import load_dotenv
from slugify import slugify

sys.path.insert(0, str(Path(__file__).parent))
from src.ai_writer import AIWriter
from src.image_fetcher import PexelsImageFetcher
from src.wp_publisher import WordPressPublisher


ROOT = Path(__file__).parent
LOG_FILE = ROOT / "posted_log.json"
TOPICS_FILE = ROOT / "topics.txt"
GENERATED_DIR = ROOT / "generated"
GENERATED_DIR.mkdir(exist_ok=True)


def load_posted_log() -> list:
    if LOG_FILE.exists():
        return json.loads(LOG_FILE.read_text())
    return []


def save_posted_log(log: list) -> None:
    LOG_FILE.write_text(json.dumps(log, indent=2))


def load_pending_topics() -> list:
    if not TOPICS_FILE.exists():
        return []
    posted_titles = {entry["topic"].lower() for entry in load_posted_log()}
    topics = []
    for line in TOPICS_FILE.read_text().splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        if line.lower() in posted_titles:
            continue
        topics.append(line)
    return topics


def load_config() -> dict:
    load_dotenv(ROOT / ".env")
    required = ["WP_SITE_URL", "WP_USERNAME", "WP_APP_PASSWORD", "GEMINI_API_KEY"]
    missing = [k for k in required if not os.getenv(k)]
    if missing:
        print(f"ERROR: Missing required env vars: {', '.join(missing)}")
        print("Copy .env.example to .env and fill in the values.")
        sys.exit(1)
    return {
        "wp_url": os.getenv("WP_SITE_URL"),
        "wp_user": os.getenv("WP_USERNAME"),
        "wp_pass": os.getenv("WP_APP_PASSWORD"),
        "gemini_key": os.getenv("GEMINI_API_KEY"),
        "gemini_model": os.getenv("GEMINI_MODEL", "gemini-2.0-flash"),
        "pexels_key": os.getenv("PEXELS_API_KEY", ""),
        "niche": os.getenv("NICHE", "General Indian audience"),
        "default_status": os.getenv("POST_STATUS", "draft"),
        "default_category": os.getenv("DEFAULT_CATEGORY", "General"),
        "default_author": int(os.getenv("DEFAULT_AUTHOR_ID", "1")),
    }


def post_one_topic(topic: str, cfg: dict, status: str) -> dict:
    print(f"\n{'='*70}\nTOPIC: {topic}\n{'='*70}")

    # 1. Generate content
    print("[1/4] Generating content with Gemini...")
    writer = AIWriter(cfg["gemini_key"], cfg["gemini_model"])
    post = writer.generate_post(topic, cfg["niche"])
    print(f"      Title: {post['title']}")
    print(f"      Words: ~{len(post['content_html'].split())}")

    # Save a local copy for review
    safe_slug = slugify(post.get("slug") or post["title"])[:60]
    (GENERATED_DIR / f"{safe_slug}.json").write_text(json.dumps(post, indent=2))

    # 2. Fetch featured image
    media_id = None
    if cfg["pexels_key"]:
        print("[2/4] Fetching featured image from Pexels...")
        fetcher = PexelsImageFetcher(cfg["pexels_key"])
        img = fetcher.search_image(post.get("image_query") or topic)
        if img:
            print(f"      Found image by {img['photographer']}")
        else:
            print("      No image found - posting without featured image")
    else:
        print("[2/4] Skipping image (no Pexels key)")
        img = None

    # 3. Connect to WP & set up taxonomy
    print("[3/4] Connecting to WordPress...")
    wp = WordPressPublisher(cfg["wp_url"], cfg["wp_user"], cfg["wp_pass"])
    user = wp.test_connection()
    print(f"      Connected as: {user.get('name')} (id={user.get('id')})")

    cat_name = post.get("category") or cfg["default_category"]
    cat_id = wp.get_or_create_category(cat_name)
    tag_ids = wp.get_or_create_tags(post.get("tags", []))

    if img:
        try:
            image_bytes = PexelsImageFetcher(cfg["pexels_key"]).download(img["url"])
            media_id = wp.upload_media(image_bytes, f"{safe_slug}.jpg", img["alt"])
            print(f"      Uploaded media id={media_id}")
        except Exception as e:
            print(f"      Image upload failed (continuing without): {e}")

    # 4. Create post
    print(f"[4/4] Creating post (status={status})...")
    created = wp.create_post(
        title=post["title"],
        content_html=post["content_html"],
        slug=post.get("slug") or safe_slug,
        excerpt=post.get("excerpt", ""),
        meta_description=post.get("meta_description", ""),
        category_ids=[cat_id],
        tag_ids=tag_ids,
        featured_media_id=media_id,
        status=status,
        author_id=cfg["default_author"],
    )
    print(f"      DONE - id={created['id']}, link={created.get('link')}")

    # Update log
    log = load_posted_log()
    log.append({
        "topic": topic,
        "title": post["title"],
        "post_id": created["id"],
        "link": created.get("link"),
        "status": status,
        "posted_at": datetime.utcnow().isoformat() + "Z",
    })
    save_posted_log(log)

    return created


def cmd_test(cfg: dict) -> None:
    print("Testing WordPress connection...")
    wp = WordPressPublisher(cfg["wp_url"], cfg["wp_user"], cfg["wp_pass"])
    user = wp.test_connection()
    print(f"  OK - logged in as {user.get('name')}")

    print("Testing Gemini API...")
    writer = AIWriter(cfg["gemini_key"], cfg["gemini_model"])
    # tiny test
    import google.generativeai as genai
    genai.configure(api_key=cfg["gemini_key"])
    m = genai.GenerativeModel(cfg["gemini_model"])
    r = m.generate_content("Reply with the single word: OK")
    print(f"  Gemini said: {r.text.strip()[:50]}")

    if cfg["pexels_key"]:
        print("Testing Pexels API...")
        f = PexelsImageFetcher(cfg["pexels_key"])
        img = f.search_image("technology")
        print(f"  OK - {'found image' if img else 'no image returned'}")
    else:
        print("Pexels key not set (optional)")
    print("\nAll connections OK. You're ready to post.")


def main():
    parser = argparse.ArgumentParser(description="Auto-post AI blog articles to WordPress")
    parser.add_argument("--count", type=int, default=1, help="Number of topics to post")
    parser.add_argument("--topic", type=str, help="Single custom topic to post")
    parser.add_argument("--status", choices=["draft", "publish"], help="Post status (overrides .env)")
    parser.add_argument("--test", action="store_true", help="Test API connections only")
    args = parser.parse_args()

    cfg = load_config()

    if args.test:
        cmd_test(cfg)
        return

    status = args.status or cfg["default_status"]
    if status == "publish":
        print("WARNING: status=publish - posts will go LIVE immediately.")
        print("For AdSense approval, 'draft' is recommended so you can review/edit each post.\n")

    if args.topic:
        topics_to_post = [args.topic]
    else:
        pending = load_pending_topics()
        if not pending:
            print("No pending topics in topics.txt (all posted, or file empty).")
            return
        topics_to_post = pending[: args.count]

    print(f"Will post {len(topics_to_post)} topic(s) as '{status}':")
    for t in topics_to_post:
        print(f"  - {t}")

    for topic in topics_to_post:
        try:
            post_one_topic(topic, cfg, status)
        except Exception as e:
            print(f"\nERROR posting '{topic}': {e}")
            continue


if __name__ == "__main__":
    main()
