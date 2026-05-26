import type { Metadata, Viewport } from "next";
import "./globals.css";
import SchemaMarkup from "@/components/SchemaMarkup";

const SITE_URL = "https://suryashakti.com"; // Client domain से बदलें

export const metadata: Metadata = {
  metadataBase: new URL(SITE_URL),
  title: {
    default:
      "SuryaShakti Solar — रूफटॉप सोलर इंस्टॉलेशन | PM Surya Ghar Yojana",
    template: "%s | SuryaShakti Solar",
  },
  description:
    "घर पर सोलर पैनल लगवाएं और हर महीने ₹3000+ बिजली बचाएं। PM Surya Ghar Yojana की ₹78,000 तक सब्सिडी, 25 साल वारंटी, मुफ्त साइट विज़िट। 5000+ खुश ग्राहक।",
  keywords: [
    "solar panel installation India",
    "rooftop solar Jaipur",
    "PM Surya Ghar Yojana",
    "solar subsidy India 2026",
    "घर पर सोलर पैनल",
    "सोलर सब्सिडी",
    "solar installer near me",
    "MNRE approved solar",
    "residential solar India",
    "commercial solar installation",
  ],
  authors: [{ name: "SuryaShakti Solar" }],
  creator: "SuryaShakti Solar",
  publisher: "SuryaShakti Solar",
  formatDetection: {
    email: false,
    address: false,
    telephone: false,
  },
  openGraph: {
    title: "SuryaShakti Solar — रूफटॉप सोलर इंस्टॉलेशन",
    description:
      "₹78,000 तक सरकारी सब्सिडी • 25 साल वारंटी • मुफ्त साइट विज़िट • 5000+ खुश ग्राहक",
    url: SITE_URL,
    siteName: "SuryaShakti Solar",
    locale: "hi_IN",
    type: "website",
    images: [
      {
        url: "/og-image.jpg",
        width: 1200,
        height: 630,
        alt: "SuryaShakti Solar - Rooftop Solar Installation in India",
      },
    ],
  },
  twitter: {
    card: "summary_large_image",
    title: "SuryaShakti Solar — Rooftop Solar Installation",
    description:
      "Save ₹3000+ monthly. ₹78,000 subsidy. 25-year warranty. 5000+ happy customers.",
    images: ["/og-image.jpg"],
  },
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      "max-video-preview": -1,
      "max-image-preview": "large",
      "max-snippet": -1,
    },
  },
  alternates: {
    canonical: SITE_URL,
    languages: {
      "hi-IN": SITE_URL,
      "en-IN": `${SITE_URL}/en`,
    },
  },
  verification: {
    google: "PLACEHOLDER_GOOGLE_VERIFICATION_CODE",
  },
};

export const viewport: Viewport = {
  themeColor: "#f57c00",
  width: "device-width",
  initialScale: 1,
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="hi">
      <head>
        <link rel="canonical" href={SITE_URL} />
        <SchemaMarkup />
      </head>
      <body>{children}</body>
    </html>
  );
}
