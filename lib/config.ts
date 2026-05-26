// ============================================
// CLIENT CONFIG — सिर्फ इस file में बदलाव करें
// ============================================
// हर client के लिए सिर्फ नीचे की values change करें।
// बाकी पूरी website automatically update हो जाएगी।

export const SITE_CONFIG = {
  // Business Details
  businessName: "SuryaShakti Solar",
  tagline: "रूफटॉप सोलर इंस्टॉलेशन | PM Surya Ghar Yojana",
  city: "Jaipur",
  state: "Rajasthan",
  fullAddress: "123, सोलर हब, जयपुर, राजस्थान 302001",

  // Contact - बहुत ज़रूरी, यहाँ से सब WhatsApp/Email जाएंगे
  whatsappNumber: "919999999999", // 91 के साथ, बिना +
  phoneNumber: "+919999999999",
  email: "info@suryashakti.com",

  // Domain
  siteUrl: "https://suryashakti.com",

  // Lead notifications
  leadEmail: "leads@suryashakti.com", // यहाँ form submissions जाएंगी

  // Social proof
  customersServed: "5000+",
  rating: "4.9",
  reviewCount: "2400",
  yearsWarranty: "25",
};

// WhatsApp message templates
export const WHATSAPP_MESSAGES = {
  general: "मुझे सोलर पैनल के बारे में जानकारी चाहिए",
  quote: "मुझे सोलर सिस्टम का कोटेशन चाहिए",
  subsidy: "मुझे PM Surya Ghar सब्सिडी के बारे में बताएं",
  amc: "मुझे AMC service चाहिए",
};

// WhatsApp link generator
export function whatsappLink(message: string = WHATSAPP_MESSAGES.general): string {
  const encoded = encodeURIComponent(message);
  return `https://wa.me/${SITE_CONFIG.whatsappNumber}?text=${encoded}`;
}

// Format lead message for WhatsApp
export function formatLeadMessage(form: {
  name: string;
  phone: string;
  city: string;
  bill?: string;
  message?: string;
}): string {
  return `🌞 *नया लीड - ${SITE_CONFIG.businessName}*

*नाम:* ${form.name}
*फोन:* ${form.phone}
*शहर:* ${form.city}
${form.bill ? `*मासिक बिल:* ₹${form.bill}` : ""}
${form.message ? `*संदेश:* ${form.message}` : ""}

_Lead from website_`;
}
