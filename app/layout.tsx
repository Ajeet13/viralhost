import type { Metadata, Viewport } from "next";
import "./globals.css";

const SITE_URL = "https://viralhost.example.com"; // अपने domain से बदलें

export const metadata: Metadata = {
  metadataBase: new URL(SITE_URL),
  title: {
    default: "Plagiarism Checker Pro — AI Detection & Deep Scan",
    template: "%s | Plagiarism Checker Pro",
  },
  description:
    "Professional plagiarism checker with AI detection, fingerprint analysis, and automatic rewriting. Supports Normal, Deep, and Academic scan modes across the entire web.",
  keywords: [
    "plagiarism checker",
    "AI detection",
    "deep scan plagiarism",
    "academic plagiarism checker",
    "fingerprint plagiarism",
    "content originality checker",
    "rewrite plagiarized text",
    "duplicate content checker",
  ],
  authors: [{ name: "Plagiarism Checker Pro" }],
  openGraph: {
    title: "Plagiarism Checker Pro — AI Detection & Deep Scan",
    description:
      "Detect plagiarism across the entire web, check AI-generated content, and auto-rewrite flagged text.",
    url: SITE_URL,
    siteName: "Plagiarism Checker Pro",
    locale: "en_US",
    type: "website",
  },
  twitter: {
    card: "summary_large_image",
    title: "Plagiarism Checker Pro",
    description:
      "AI-powered plagiarism detection with Deep & Academic scan modes and automatic rewriting.",
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
  },
};

export const viewport: Viewport = {
  themeColor: "#4f46e5",
  width: "device-width",
  initialScale: 1,
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
