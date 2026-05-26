// JSON-LD structured data — Google को business समझाने के लिए
// SEO में बहुत बड़ा फायदा देता है (rich snippets, knowledge panel)

export default function SchemaMarkup() {
  const businessSchema = {
    "@context": "https://schema.org",
    "@type": "LocalBusiness",
    "@id": "https://suryashakti.com",
    name: "SuryaShakti Solar",
    image: "https://suryashakti.com/logo.png",
    url: "https://suryashakti.com",
    telephone: "+91-99999-99999",
    email: "info@suryashakti.com",
    priceRange: "₹₹",
    address: {
      "@type": "PostalAddress",
      streetAddress: "123, Solar Hub",
      addressLocality: "Jaipur",
      addressRegion: "Rajasthan",
      postalCode: "302001",
      addressCountry: "IN",
    },
    geo: {
      "@type": "GeoCoordinates",
      latitude: 26.9124,
      longitude: 75.7873,
    },
    openingHoursSpecification: {
      "@type": "OpeningHoursSpecification",
      dayOfWeek: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
      opens: "09:00",
      closes: "19:00",
    },
    aggregateRating: {
      "@type": "AggregateRating",
      ratingValue: "4.9",
      reviewCount: "2400",
    },
    areaServed: [
      { "@type": "State", name: "Rajasthan" },
      { "@type": "State", name: "Uttar Pradesh" },
      { "@type": "State", name: "Gujarat" },
      { "@type": "State", name: "Madhya Pradesh" },
    ],
  };

  const serviceSchema = {
    "@context": "https://schema.org",
    "@type": "Service",
    serviceType: "Solar Panel Installation",
    provider: {
      "@type": "LocalBusiness",
      name: "SuryaShakti Solar",
    },
    areaServed: { "@type": "Country", name: "India" },
    hasOfferCatalog: {
      "@type": "OfferCatalog",
      name: "Solar Services",
      itemListElement: [
        {
          "@type": "Offer",
          itemOffered: {
            "@type": "Service",
            name: "Residential Rooftop Solar Installation",
          },
          price: "65000",
          priceCurrency: "INR",
        },
        {
          "@type": "Offer",
          itemOffered: {
            "@type": "Service",
            name: "Commercial Solar Installation",
          },
        },
        {
          "@type": "Offer",
          itemOffered: {
            "@type": "Service",
            name: "Solar Subsidy Application Assistance",
          },
        },
      ],
    },
  };

  const faqSchema = {
    "@context": "https://schema.org",
    "@type": "FAQPage",
    mainEntity: [
      {
        "@type": "Question",
        name: "PM Surya Ghar Yojana में कितनी सब्सिडी मिलती है?",
        acceptedAnswer: {
          "@type": "Answer",
          text: "1 kW पर ₹30,000, 2 kW पर ₹60,000, और 3 kW या उससे ज़्यादा पर अधिकतम ₹78,000 सब्सिडी मिलती है।",
        },
      },
      {
        "@type": "Question",
        name: "सोलर पैनल लगवाने में कितना समय लगता है?",
        acceptedAnswer: {
          "@type": "Answer",
          text: "रजिस्ट्रेशन से लेकर इंस्टॉलेशन और नेट मीटरिंग तक 30-45 दिन लगते हैं। पैनल लगाने का असली काम सिर्फ 2-3 दिन में पूरा होता है।",
        },
      },
      {
        "@type": "Question",
        name: "सोलर पैनल की वारंटी कितने साल की होती है?",
        acceptedAnswer: {
          "@type": "Answer",
          text: "सोलर पैनल पर 25 साल की वारंटी मिलती है। इन्वर्टर पर 5-10 साल की वारंटी होती है।",
        },
      },
      {
        "@type": "Question",
        name: "क्या सब्सिडी सीधे बैंक खाते में आती है?",
        acceptedAnswer: {
          "@type": "Answer",
          text: "हाँ, इंस्टॉलेशन और नेट मीटरिंग के बाद 30 दिन के अंदर सब्सिडी सीधे आपके बैंक खाते में DBT से आ जाती है।",
        },
      },
    ],
  };

  return (
    <>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(businessSchema) }}
      />
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(serviceSchema) }}
      />
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(faqSchema) }}
      />
    </>
  );
}
