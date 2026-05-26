# 🌞 Solar Installer Website Template

A production-ready, SEO-optimized, Hindi-first solar installation business website.
**आप इसे ₹25,000 - ₹50,000 में clients को बेच सकते हैं।**

## Features

- ✅ Modern Hindi UI (mobile responsive)
- ✅ Interactive Savings Calculator
- ✅ WhatsApp + Email lead capture
- ✅ Full SEO (sitemap, schema, OG tags)
- ✅ PM Surya Ghar Yojana focus
- ✅ Single config file customization
- ✅ Free Vercel deployment

## Quick Start (Local)

```bash
npm install
npm run dev
```

Open `http://localhost:3000`

## Customize for Each Client (5 minutes)

Edit ONLY this file: `lib/config.ts`

```typescript
export const SITE_CONFIG = {
  businessName: "Client Solar Co",
  whatsappNumber: "919999999999", // Client's number
  phoneNumber: "+919999999999",
  email: "info@client.com",
  city: "Jaipur",
  // ...
};
```

That's it! पूरी website automatically update हो जाएगी।

## Deploy to Vercel (Free, 2 minutes)

1. Push code to GitHub
2. Go to vercel.com → Import repo
3. Click Deploy → Done!

Custom domain: Add via Vercel dashboard (Settings → Domains)

## Optional: Lead Capture Setup

### Email (Resend - 3000 free/month)

1. Sign up at [resend.com](https://resend.com)
2. Get API key
3. Add to Vercel env: `RESEND_API_KEY=re_xxx`

### Google Sheets (100% Free)

1. Create new Google Sheet
2. Extensions → Apps Script → paste this:

```javascript
function doPost(e) {
  const sheet = SpreadsheetApp.getActiveSheet();
  const data = JSON.parse(e.postData.contents);
  sheet.appendRow([
    new Date(), data.name, data.phone, data.city, data.bill, data.message
  ]);
  return ContentService.createTextOutput(JSON.stringify({success: true}));
}
```

3. Deploy → New deployment → Web app → Anyone → Deploy
4. Copy URL → Add to Vercel env: `GOOGLE_SHEETS_WEBHOOK_URL=https://...`

## Tech Stack

- Next.js 14 (App Router)
- TypeScript
- Tailwind CSS
- Lucide Icons
- Vercel (hosting)

## Project Structure

```
solar-website-template/
├── app/
│   ├── api/lead/route.ts    # Lead capture API
│   ├── layout.tsx           # SEO metadata
│   ├── page.tsx             # Main page
│   ├── sitemap.ts           # SEO sitemap
│   └── robots.ts            # SEO robots
├── components/
│   ├── Navbar.tsx
│   ├── Hero.tsx
│   ├── Services.tsx
│   ├── Calculator.tsx       # Savings calculator
│   ├── Subsidy.tsx          # PM Surya Ghar info
│   ├── Testimonials.tsx
│   ├── ContactForm.tsx      # Lead capture form
│   ├── Footer.tsx
│   ├── WhatsAppFloat.tsx
│   └── SchemaMarkup.tsx     # JSON-LD SEO
└── lib/
    └── config.ts            # ⭐ Edit this for each client
```

## License

Yours to sell as a service. एक template = कई clients को बेचो।
