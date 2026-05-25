"""
AdSense readiness toolkit:
  python adsense_setup.py check       # check site readiness
  python adsense_setup.py pages       # create About / Contact / Privacy / Terms / Disclaimer pages

Required legal pages are generated from generic templates with your site name filled in.
You should review and customize them before publishing.
"""
import argparse
import os
import sys
from pathlib import Path
from urllib.parse import urlparse

import requests
from dotenv import load_dotenv

sys.path.insert(0, str(Path(__file__).parent))
from src.wp_publisher import WordPressPublisher


def site_name_from_url(url: str) -> str:
    host = urlparse(url).netloc or url
    return host.replace("www.", "").split(".")[0].capitalize()


PAGE_TEMPLATES = {
    "about": {
        "title": "About Us",
        "slug": "about",
        "content": """<p>Welcome to <strong>{site}</strong> ({url}).</p>
<p>{site} is a blog dedicated to providing practical, well-researched content
on careers, personal finance, and skill-building for young Indians. Our goal is
to help readers make informed decisions in their professional and financial lives.</p>
<h2>What We Cover</h2>
<ul>
  <li>Career guidance and job search tips</li>
  <li>Personal finance, savings, and tax planning</li>
  <li>Investment basics for beginners</li>
  <li>Skill development and learning resources</li>
</ul>
<h2>Our Approach</h2>
<p>Every article is written or reviewed by a human editor. We use general public
information and our own analysis. The content is for educational purposes only and
does not constitute professional advice.</p>
<h2>Contact</h2>
<p>Have feedback or a topic suggestion? Visit our <a href="/contact/">Contact page</a>.</p>
""",
    },
    "contact": {
        "title": "Contact Us",
        "slug": "contact",
        "content": """<p>We would love to hear from you. Reach out for feedback, corrections,
collaborations, or topic suggestions.</p>
<p><strong>Email:</strong> contact@{domain}</p>
<p>We typically respond within 2-3 business days.</p>
<h2>Editorial Corrections</h2>
<p>If you spot a factual error in any article, please email us with the article URL
and details. We take accuracy seriously and will issue corrections as needed.</p>
""",
    },
    "privacy": {
        "title": "Privacy Policy",
        "slug": "privacy-policy",
        "content": """<p><em>Last updated: {today}</em></p>
<p>This Privacy Policy describes how <strong>{site}</strong> ({url}) collects, uses,
and protects information when you visit this website.</p>

<h2>Information We Collect</h2>
<ul>
  <li><strong>Log data:</strong> IP address, browser type, pages visited, time spent.</li>
  <li><strong>Cookies:</strong> small files stored by your browser to improve your experience.</li>
  <li><strong>Voluntary information:</strong> name and email if you contact us or subscribe.</li>
</ul>

<h2>How We Use Information</h2>
<ul>
  <li>To operate and improve the website.</li>
  <li>To respond to your inquiries.</li>
  <li>To analyze traffic and content performance.</li>
</ul>

<h2>Third-Party Services</h2>
<p>This site may use the following third-party services that collect information:</p>
<ul>
  <li><strong>Google Analytics</strong> - traffic measurement.</li>
  <li><strong>Google AdSense</strong> - advertising. Google may use cookies to serve
  ads based on your prior visits to this and other websites. You may opt out of
  personalized advertising by visiting
  <a href="https://www.google.com/settings/ads" rel="noopener" target="_blank">Google Ads Settings</a>.</li>
</ul>

<h2>Cookies</h2>
<p>You can disable cookies through your browser settings. Some site features may
not function properly without cookies.</p>

<h2>Your Rights</h2>
<p>You may request access, correction, or deletion of any personal information we
hold about you by emailing contact@{domain}.</p>

<h2>Children's Privacy</h2>
<p>This site is not directed at children under 13. We do not knowingly collect
information from children under 13.</p>

<h2>Changes</h2>
<p>We may update this policy from time to time. Material changes will be posted
on this page with a new "last updated" date.</p>
""",
    },
    "terms": {
        "title": "Terms and Conditions",
        "slug": "terms-and-conditions",
        "content": """<p><em>Last updated: {today}</em></p>
<p>By accessing <strong>{site}</strong> ({url}) you agree to these Terms.</p>

<h2>Use of Content</h2>
<p>All articles, images, and graphics are the property of {site} unless otherwise
attributed. You may share links and short excerpts with proper attribution. You
may not republish full articles without written permission.</p>

<h2>Accuracy</h2>
<p>We make reasonable efforts to keep information accurate and up to date, but
we make no warranties about completeness or reliability. Use the information at
your own risk.</p>

<h2>External Links</h2>
<p>Articles may link to external websites. We are not responsible for the content
or practices of those sites.</p>

<h2>Limitation of Liability</h2>
<p>{site} is not liable for any direct, indirect, or consequential damages arising
from the use of this website or its content.</p>

<h2>Governing Law</h2>
<p>These Terms are governed by the laws of India. Any disputes shall be subject
to the exclusive jurisdiction of the courts of India.</p>

<h2>Contact</h2>
<p>Questions about these Terms? Email contact@{domain}.</p>
""",
    },
    "disclaimer": {
        "title": "Disclaimer",
        "slug": "disclaimer",
        "content": """<p><em>Last updated: {today}</em></p>
<p>The information on <strong>{site}</strong> ({url}) is for general informational
and educational purposes only.</p>

<h2>Not Professional Advice</h2>
<p>Articles related to finance, investments, taxes, careers, or health are not a
substitute for professional advice. Always consult a qualified professional before
making decisions based on the content of this website.</p>

<h2>No Guarantees</h2>
<p>We make no representations about the accuracy or completeness of information.
Any action you take based on the information on this site is strictly at your own risk.</p>

<h2>Affiliate Disclosure</h2>
<p>Some posts may contain affiliate links. If you purchase through these links, we
may receive a small commission at no additional cost to you. This does not influence
our editorial content.</p>

<h2>External Sources</h2>
<p>External links are provided for convenience and informational purposes. We do
not endorse or take responsibility for content on linked sites.</p>
""",
    },
}


def get_wp() -> WordPressPublisher:
    load_dotenv()
    for k in ("WP_SITE_URL", "WP_USERNAME", "WP_APP_PASSWORD"):
        if not os.getenv(k):
            print(f"Missing {k} in .env")
            sys.exit(1)
    return WordPressPublisher(
        os.getenv("WP_SITE_URL"),
        os.getenv("WP_USERNAME"),
        os.getenv("WP_APP_PASSWORD"),
    )


def cmd_pages():
    from datetime import date
    wp = get_wp()
    site_url = os.getenv("WP_SITE_URL").rstrip("/")
    site = site_name_from_url(site_url)
    domain = urlparse(site_url).netloc.replace("www.", "")
    today = date.today().strftime("%B %d, %Y")

    for key, tpl in PAGE_TEMPLATES.items():
        content = tpl["content"].format(site=site, url=site_url, domain=domain, today=today)
        try:
            created = wp.create_page(tpl["title"], content, tpl["slug"], status="publish")
            print(f"Created: {tpl['title']:30} -> {created.get('link')}")
        except Exception as e:
            msg = str(e)
            if "term_exists" in msg or "already exists" in msg or "duplicate" in msg.lower():
                print(f"Skipped: {tpl['title']:30} (already exists)")
            else:
                print(f"Failed:  {tpl['title']:30} -> {e}")


def cmd_check():
    load_dotenv()
    site_url = os.getenv("WP_SITE_URL", "").rstrip("/")
    if not site_url:
        print("WP_SITE_URL not set in .env")
        return
    print(f"Checking AdSense readiness for: {site_url}\n")

    checks = []

    # 1. site reachable + HTTPS
    try:
        r = requests.get(site_url, timeout=20)
        checks.append(("Site reachable", r.ok, f"HTTP {r.status_code}"))
        checks.append(("Uses HTTPS", site_url.startswith("https://"), site_url.split("://")[0]))
    except Exception as e:
        checks.append(("Site reachable", False, str(e)))

    # 2. WP REST API
    try:
        r = requests.get(f"{site_url}/wp-json/wp/v2/posts?per_page=100", timeout=20)
        post_count = len(r.json()) if r.ok else 0
        checks.append((
            "Published posts (>= 20 recommended)",
            post_count >= 20,
            f"{post_count} posts found"
        ))
    except Exception as e:
        checks.append(("Published posts", False, str(e)))

    # 3. required pages
    for slug in ["about", "contact", "privacy-policy", "terms-and-conditions", "disclaimer"]:
        try:
            r = requests.get(f"{site_url}/{slug}/", timeout=15, allow_redirects=True)
            ok = r.ok and r.status_code == 200
            checks.append((f"Page exists: /{slug}/", ok, f"HTTP {r.status_code}"))
        except Exception as e:
            checks.append((f"Page exists: /{slug}/", False, str(e)))

    # 4. ads.txt
    try:
        r = requests.get(f"{site_url}/ads.txt", timeout=15)
        checks.append(("ads.txt accessible", r.ok, f"HTTP {r.status_code}"))
    except Exception as e:
        checks.append(("ads.txt accessible", False, str(e)))

    # 5. sitemap
    try:
        r = requests.get(f"{site_url}/sitemap.xml", timeout=15, allow_redirects=True)
        checks.append(("Sitemap accessible", r.ok, f"HTTP {r.status_code}"))
    except Exception as e:
        checks.append(("Sitemap accessible", False, str(e)))

    # print results
    print(f"{'Check':<45} {'Status':<8} Details")
    print("-" * 80)
    passed = 0
    for name, ok, details in checks:
        mark = "PASS" if ok else "FAIL"
        if ok:
            passed += 1
        print(f"{name:<45} {mark:<8} {details}")
    print("-" * 80)
    print(f"\n{passed}/{len(checks)} checks passed.")
    if passed == len(checks):
        print("Looks good. You can apply at https://www.google.com/adsense/start/")
    else:
        print("Fix the FAILED items above before applying for AdSense.")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("command", choices=["check", "pages"])
    args = parser.parse_args()
    if args.command == "check":
        cmd_check()
    elif args.command == "pages":
        cmd_pages()


if __name__ == "__main__":
    main()
