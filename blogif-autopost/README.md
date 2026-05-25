# blogif-autopost

Auto-post AI-generated blog articles to your WordPress site (`blogif.in`) and prepare it for Google AdSense approval.

## What this does

1. Reads topics from `topics.txt`.
2. Uses **Google Gemini** to generate a full SEO-optimized article (title, slug, meta description, tags, FAQ, HTML content).
3. Fetches a free featured image from **Pexels**.
4. Posts everything to your WordPress site via the REST API (as a **draft** by default).
5. Tracks posted topics in `posted_log.json` so duplicates are skipped.

Plus a separate `adsense_setup.py` tool that:
- Generates required AdSense pages (About / Contact / Privacy / Terms / Disclaimer).
- Runs a readiness checklist against your live site.

---

## IMPORTANT: AdSense Reality Check

Google explicitly disapproves "scaled content abuse" - sites mass-publishing pure AI content get **rejected or banned**. To pass AdSense review:

- **Always post as `draft` first.** Read every article. Edit it. Add personal touches, fix errors, add real examples.
- **Post 1-2 articles per day**, not 20. Slow growth looks natural.
- **Add real value.** Indian context, current data (2026), your own opinions, real screenshots where useful.
- **Wait 2-4 weeks** after reaching ~25 quality posts before applying to AdSense.

This tool is a **content assistant**, not a "press button -> get approved" machine. Anyone selling that is a scam.

---

## Setup (one-time)

### 1. Get the required keys (all have free tiers)

| Service | Where to get | Cost |
|---|---|---|
| Google Gemini API | https://aistudio.google.com/app/apikey | Free tier: 60 req/min |
| Pexels API | https://www.pexels.com/api/ | Free, unlimited |
| WordPress App Password | WP Admin -> Users -> Profile -> Application Passwords (scroll to bottom) | Free |

**WordPress Application Password steps:**
1. Log in to `https://blogif.in/wp-admin`.
2. Go to **Users -> Profile** (or Users -> All Users -> click your user).
3. Scroll to **Application Passwords** at the bottom.
4. Type a name like `autopost` and click **Add New Application Password**.
5. **Copy the password shown** (it has spaces - keep them). You only see it once.

### 2. Install Python dependencies

```bash
cd blogif-autopost
python -m venv .venv
source .venv/bin/activate          # Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

### 3. Configure `.env`

```bash
cp .env.example .env
# Edit .env and fill in: WP_USERNAME, WP_APP_PASSWORD, GEMINI_API_KEY, PEXELS_API_KEY
```

### 4. Test connections

```bash
python run.py --test
```

You should see green-ish output for WordPress, Gemini, and Pexels.

---

## Usage

### Generate required AdSense pages (run once)

```bash
python adsense_setup.py pages
```

This creates About, Contact, Privacy Policy, Terms, and Disclaimer pages on your site. **Open each one in WP Admin and customize it** (especially the email addresses and About section - generic content gets flagged).

### Auto-post articles

```bash
# Post 1 draft from topics.txt
python run.py

# Post 3 drafts at once
python run.py --count 3

# Post a custom topic
python run.py --topic "How to invest in NPS for tax savings"

# Publish live (only after manual review!)
python run.py --status publish --topic "..."
```

After running, check `https://blogif.in/wp-admin/edit.php?post_status=draft` to review the drafts. **Edit each one before publishing.**

### Check AdSense readiness

```bash
python adsense_setup.py check
```

Runs against your live site and tells you which AdSense criteria are met or missing.

---

## Recommended workflow for AdSense approval

| Week | Action |
|---|---|
| Week 1 | Run `adsense_setup.py pages`. Manually customize each page. Add a real `Contact` email (your own). |
| Week 1 | Run `python run.py --count 1` daily. Review and edit each draft, then publish. |
| Weeks 2-3 | Continue 1-2 posts/day. Aim for 20-25 posts total. Add 2-3 internal links per post. |
| Week 4 | Run `python adsense_setup.py check`. Fix any FAILs. |
| Week 4 | Apply for AdSense at https://www.google.com/adsense/start/. |
| Wait | Review takes 1-4 weeks. Keep posting during this time. |

---

## File overview

```
blogif-autopost/
  .env.example          # template for your secrets
  topics.txt            # one topic per line - edit freely
  requirements.txt
  run.py                # main: generate + post articles
  adsense_setup.py      # readiness checker + legal pages
  src/
    ai_writer.py        # Gemini content generation
    image_fetcher.py    # Pexels images
    wp_publisher.py     # WordPress REST API client
  generated/            # local JSON copy of every generated article (auto-created)
  posted_log.json       # tracks what's been posted (auto-created)
```

---

## Troubleshooting

**`WP auth failed [401]`**
Application password is wrong, or your WP host blocks Basic Auth. Try the [Application Passwords plugin](https://wordpress.org/plugins/application-passwords/) or contact your host.

**`Gemini returned invalid JSON`**
Re-run. Occasional flake. If persistent, lower `temperature` in `src/ai_writer.py`.

**Featured image upload fails**
Some hosts block direct media uploads via REST. Set `PEXELS_API_KEY=` empty to disable, or upload images manually.

**`429 Too Many Requests` from Gemini**
Free tier limit. Wait a minute or upgrade.

---

## Costs

Running this tool itself is **free** (all APIs have free tiers sufficient for 1-3 posts/day). Your only cost is:
- Domain `blogif.in` (already paid)
- WordPress hosting (already paid)

If you want zero-cost AI generation forever, stick with Gemini's free tier (`gemini-2.0-flash`).
