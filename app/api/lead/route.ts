import { NextRequest, NextResponse } from "next/server";
import { SITE_CONFIG } from "@/lib/config";

// ============================================
// Lead Capture API
// ============================================
// यह API form submissions को process करती है।
// 3 जगह leads भेज सकते हैं:
// 1. Email (Resend/SendGrid - free tier)
// 2. WhatsApp (via webhook - paid but optional)
// 3. Google Sheets (free, simplest)

export async function POST(req: NextRequest) {
  try {
    const data = await req.json();
    const { name, phone, city, bill, message, source } = data;

    // Basic validation
    if (!name || !phone || !city) {
      return NextResponse.json(
        { error: "नाम, फोन और शहर ज़रूरी हैं" },
        { status: 400 }
      );
    }

    // Phone format check (10 digits Indian)
    if (!/^[6-9]\d{9}$/.test(phone.replace(/\s+/g, ""))) {
      return NextResponse.json(
        { error: "वैध मोबाइल नंबर डालें" },
        { status: 400 }
      );
    }

    const lead = {
      name,
      phone,
      city,
      bill: bill || "Not specified",
      message: message || "",
      source: source || "website",
      timestamp: new Date().toISOString(),
    };

    // Method 1: Send to Google Sheets (FREE - recommended)
    // Setup: https://docs.google.com/.../scripts (10 min setup)
    const SHEETS_WEBHOOK = process.env.GOOGLE_SHEETS_WEBHOOK_URL;
    if (SHEETS_WEBHOOK) {
      try {
        await fetch(SHEETS_WEBHOOK, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(lead),
        });
      } catch (e) {
        console.error("Sheets save failed:", e);
      }
    }

    // Method 2: Send Email (using Resend - 3000 free emails/month)
    const RESEND_API_KEY = process.env.RESEND_API_KEY;
    if (RESEND_API_KEY) {
      try {
        await fetch("https://api.resend.com/emails", {
          method: "POST",
          headers: {
            Authorization: `Bearer ${RESEND_API_KEY}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            from: `${SITE_CONFIG.businessName} <leads@${new URL(SITE_CONFIG.siteUrl).hostname}>`,
            to: SITE_CONFIG.leadEmail,
            subject: `🌞 नया लीड: ${name} - ${city}`,
            html: `
              <h2>नया लीड मिला!</h2>
              <p><b>नाम:</b> ${name}</p>
              <p><b>फोन:</b> <a href="tel:${phone}">${phone}</a></p>
              <p><b>WhatsApp:</b> <a href="https://wa.me/91${phone}">91${phone}</a></p>
              <p><b>शहर:</b> ${city}</p>
              <p><b>मासिक बिल:</b> ₹${bill || "Not specified"}</p>
              <p><b>संदेश:</b> ${message || "—"}</p>
              <p><b>Source:</b> ${source}</p>
              <hr/>
              <p><i>${new Date().toLocaleString("hi-IN")}</i></p>
            `,
          }),
        });
      } catch (e) {
        console.error("Email failed:", e);
      }
    }

    return NextResponse.json({
      success: true,
      message: "धन्यवाद! हम जल्द संपर्क करेंगे।",
    });
  } catch (error) {
    console.error("Lead API error:", error);
    return NextResponse.json(
      { error: "कुछ गलत हुआ। कृपया WhatsApp पर संपर्क करें।" },
      { status: 500 }
    );
  }
}
