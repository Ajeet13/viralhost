# 🚀 Complete Deployment Guide (हिंदी में)

यह guide आपको step-by-step बताएगी कि website को Vercel पर FREE host कैसे करें और client को handover कैसे करें।

---

## Part 1: Local Setup (पहली बार)

### Step 1: Software Install करें

1. **Node.js** install करें: https://nodejs.org (LTS version)
2. **Git** install करें: https://git-scm.com
3. **VS Code** install करें: https://code.visualstudio.com
4. **GitHub account** बनाएं: https://github.com (free)

### Step 2: Project Run करें

Terminal/Command Prompt खोलें और project folder में जाएं:

```bash
npm install
npm run dev
```

Browser में `http://localhost:3000` खोलें — website live दिखेगी।

---

## Part 2: Client के लिए Customize करें (5 मिनट)

### सिर्फ एक file edit करनी है: `lib/config.ts`

```typescript
export const SITE_CONFIG = {
  // बदलें ⬇️
  businessName: "Client का Business Name",
  tagline: "Client की tagline",
  city: "Client का शहर",
  state: "State",
  fullAddress: "पूरा address",
  
  whatsappNumber: "919999999999", // Client का WhatsApp (91 के साथ)
  phoneNumber: "+919999999999",
  email: "client@email.com",
  
  siteUrl: "https://clientdomain.com",
  leadEmail: "leads@clientdomain.com",
  
  customersServed: "5000+",
  rating: "4.9",
  reviewCount: "2400",
  yearsWarranty: "25",
};
```

**बस! पूरी website automatic update हो जाएगी।**

---

## Part 3: GitHub पर Code Upload करें

```bash
git init
git add .
git commit -m "Initial commit"
```

GitHub पर new repository बनाएं → Copy URL → Terminal में:

```bash
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git
git push -u origin main
```

---

## Part 4: Vercel पर Deploy (FREE)

### Step 1: Vercel Account
1. जाएं: **vercel.com**
2. **Sign up with GitHub** click करें
3. Free account बन जाएगा

### Step 2: Import Project
1. Dashboard पर **"Add New Project"** click करें
2. अपनी GitHub repo select करें
3. **Deploy** button दबाएं
4. 2 मिनट में website live हो जाएगी

आपको URL मिलेगा: `https://your-project.vercel.app`

### Step 3: Environment Variables (Optional)
Vercel Dashboard → Project → Settings → Environment Variables:

```
RESEND_API_KEY=re_xxxxxxxxxxxxx
GOOGLE_SHEETS_WEBHOOK_URL=https://script.google.com/...
```

---

## Part 5: Client के Domain से Connect करें

### Step 1: Domain खरीदें (अगर नहीं है)
- **GoDaddy** (₹100-700/year)
- **Namecheap** ($10/year)
- **Cloudflare** (cheapest, $9/year)

### Step 2: Vercel में Add करें
1. Vercel Dashboard → Project → **Settings → Domains**
2. Domain डालें: `clientdomain.com`
3. Vercel आपको DNS records देगा

### Step 3: DNS Configure करें
Domain provider के control panel में जाएं → DNS Settings:

```
Type: A
Name: @
Value: 76.76.21.21

Type: CNAME
Name: www
Value: cname.vercel-dns.com
```

5-30 मिनट में domain काम करने लगेगा।

### Step 4: SSL Certificate
Vercel automatic free SSL देगा। कुछ नहीं करना।

---

## Part 6: Lead Capture Setup (Optional, but Recommended)

### Option A: Google Sheets (100% Free)

1. **Google Sheet बनाएं** "Solar Leads" नाम से
2. Headers add करें: `Date | Name | Phone | City | Bill | Message`
3. **Extensions → Apps Script** पर click करें
4. यह code paste करें:

```javascript
function doPost(e) {
  const sheet = SpreadsheetApp.getActiveSheet();
  const data = JSON.parse(e.postData.contents);
  sheet.appendRow([
    new Date(),
    data.name,
    data.phone,
    data.city,
    data.bill || '',
    data.message || ''
  ]);
  return ContentService.createTextOutput(JSON.stringify({success: true}))
    .setMimeType(ContentService.MimeType.JSON);
}
```

5. **Deploy → New deployment → Web app**
   - Execute as: Me
   - Who has access: Anyone
6. **Deploy** → URL copy करें
7. Vercel में env variable add करें: `GOOGLE_SHEETS_WEBHOOK_URL=...`

अब हर form submission Google Sheet में automatically save होगी!

### Option B: Email Notifications (Resend - 3000 free/month)

1. Sign up: **resend.com**
2. **API Keys** → Create new key
3. Vercel में add: `RESEND_API_KEY=re_xxx`
4. हर lead पर email आएगा

---

## Part 7: Google पर Index करवाएं (SEO)

### Step 1: Google Search Console
1. जाएं: **search.google.com/search-console**
2. **Add property** → URL डालें
3. Verify करें (HTML tag method)

### Step 2: Sitemap Submit करें
1. Search Console → Sitemaps
2. Submit: `https://clientdomain.com/sitemap.xml`
3. Google 7-15 दिन में index कर देगा

### Step 3: Google My Business
**बहुत ज़रूरी!** Local search में आना है तो:
1. **business.google.com** पर business list करें
2. Address, phone, photos add करें
3. Reviews collect करें (clients से)

---

## Part 8: Testing Checklist

Website handover से पहले check करें:

- [ ] सारे buttons और links काम कर रहे हैं
- [ ] WhatsApp button सही number पर redirect करता है
- [ ] Form submit होने पर WhatsApp खुलता है
- [ ] Calculator के सही values आ रहे हैं
- [ ] Mobile पर अच्छा दिखता है (Chrome DevTools)
- [ ] सब्सिडी section की information सही है
- [ ] Footer में सब links काम करते हैं
- [ ] Page load 3 seconds से कम
- [ ] Lighthouse score 90+ (Chrome DevTools → Lighthouse)
- [ ] सारे Hindi text सही हैं

---

## Part 9: Client Handover Document

Client को यह दें:

### A. Live URLs
- Website: `https://clientdomain.com`
- Admin (Vercel): आपके पास रहेगा
- Lead Sheet: Google Sheet का link share करें

### B. Login Details (अगर client को चाहिए)
- Vercel access: Add team member as Viewer
- Domain: Client के नाम पर registration करवाएं
- Google Sheet: Editor access

### C. Maintenance Plan
हर महीने ₹2,000-5,000 charge करें इसके लिए:
- Content updates (नए reviews, photos)
- SEO monitoring
- Bug fixes
- Domain/hosting renewal

---

## Common Issues & Solutions

### Issue 1: "npm install" fails
**Solution:** Node version check करें (16+)
```bash
node --version
```

### Issue 2: Vercel deployment fails
**Solution:** Build logs check करें, अक्सर TypeScript error होती है

### Issue 3: Custom domain not working
**Solution:** DNS propagation में 24 hours लग सकते हैं। Wait करें।

### Issue 4: Form submit नहीं हो रहा
**Solution:** Browser console में error check करें (F12)

### Issue 5: SEO में show नहीं हो रही
**Solution:** Google को 1-2 हफ्ते लगते हैं। Search Console में check करें।

---

## Help चाहिए?

बस मुझसे पूछें:
- Code में किसी section को कैसे change करें
- नया feature कैसे add करें
- Multiple clients के लिए कैसे scale करें
- अगला niche template (Doctor/Restaurant/etc.)
