import type { MetadataRoute } from "next";

export default function sitemap(): MetadataRoute.Sitemap {
  const baseUrl = "https://viralhost.example.com"; // अपने domain से बदलें
  const lastModified = new Date();

  return [
    { url: baseUrl, lastModified, changeFrequency: "weekly", priority: 1 },
    {
      url: `${baseUrl}/plagiarism-checker`,
      lastModified,
      changeFrequency: "weekly",
      priority: 0.9,
    },
  ];
}
