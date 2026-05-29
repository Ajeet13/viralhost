# 🛡️ Plagiarism Checker Pro

A professional, AI-powered plagiarism checker built with Next.js. Detects duplicate content across the web, checks for AI-generated text, and automatically rewrites flagged passages.

## Features

- ✅ **Scan Modes** — Normal Scan, Deep Scan (PRO), Academic Scan (PRO)
- ✅ **Exclusions** — Exclude Quotes (PRO), Exclude Bibliography (PRO)
- ✅ **AI & Integrity** — AI Detection with probability scoring, Fingerprint (PRO)
- ✅ **Auto-Rewriting** — Replaces plagiarized sentences with unique, meaning-preserving alternatives
- ✅ **Results Dashboard** — Unique-content score, plagiarism %, sources found, scan time
- ✅ **Copy & Download** — Export the rewritten, plagiarism-free text

## Quick Start (Local)

```bash
npm install
npm run dev
```

Open `http://localhost:3000`

## Deploy to Vercel (Free, 2 minutes)

1. Push code to GitHub
2. Go to vercel.com → Import repo
3. Click Deploy → Done!

Custom domain: Add via Vercel dashboard (Settings → Domains)

## Tech Stack

- Next.js 14 (App Router)
- TypeScript
- Tailwind CSS
- Lucide Icons
- Vercel (hosting)

## Project Structure

```
viralhost/
├── app/
│   ├── api/plagiarism/route.ts        # Scanning engine, paraphrasing, AI detection
│   ├── plagiarism-checker/page.tsx    # /plagiarism-checker route
│   ├── layout.tsx                     # SEO metadata
│   ├── page.tsx                       # Home page (renders the checker)
│   ├── sitemap.ts                     # SEO sitemap
│   └── robots.ts                      # SEO robots
└── components/
    └── plagiarism/
        └── PlagiarismChecker.tsx      # Full UI component
```

## How It Works

1. Paste text (minimum 20 words)
2. Choose a scan mode and toggle PRO features
3. Click **Check Plagiarism** to scan
4. Review results across three tabs:
   - **Plagiarism Results** — flagged lines, source URLs, and suggested replacements
   - **AI Detection** — AI probability, human-content score, fingerprint, detected patterns
   - **Rewritten Text** — a fully unique version of your content

> Note: The scanning backend is a self-contained simulation/heuristic engine. To connect a live web index, replace the source-matching logic in `app/api/plagiarism/route.ts` with a real search/plagiarism API.
