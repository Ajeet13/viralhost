import type { MetadataRoute } from "next";

export default function sitemap(): MetadataRoute.Sitemap {
  const baseUrl = "https://suryashakti.com"; // Client के domain से replace करें
  const lastModified = new Date();

  return [
    { url: baseUrl, lastModified, changeFrequency: "weekly", priority: 1 },
    { url: `${baseUrl}/#services`, lastModified, changeFrequency: "monthly", priority: 0.9 },
    { url: `${baseUrl}/#calculator`, lastModified, changeFrequency: "monthly", priority: 0.9 },
    { url: `${baseUrl}/#subsidy`, lastModified, changeFrequency: "monthly", priority: 0.8 },
    { url: `${baseUrl}/#testimonials`, lastModified, changeFrequency: "monthly", priority: 0.7 },
    { url: `${baseUrl}/#contact`, lastModified, changeFrequency: "monthly", priority: 0.8 },
  ];
}
