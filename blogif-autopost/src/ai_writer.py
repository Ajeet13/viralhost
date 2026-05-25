"""
AI content generator using Google Gemini.
Generates SEO-optimized, AdSense-friendly blog posts from a topic.
"""
import json
import re
import google.generativeai as genai


SYSTEM_PROMPT = """You are an expert blog writer creating ORIGINAL, high-quality articles
for an Indian audience. Your writing must be AdSense-policy-compliant.

STRICT RULES:
1. Write in clear, conversational English (Indian context, INR currency, Indian examples).
2. NO copied content. NO fluff. Every paragraph must add real value.
3. NO medical/legal/financial professional advice - add disclaimers when relevant.
4. Use real, verifiable facts. If unsure, say "as of [year], typically..."
5. Use H2 and H3 headings, short paragraphs (2-4 sentences), bullet lists, tables when useful.
6. Add a personal/practical angle - what should the reader DO?
7. Length: 1200-1800 words.
8. NO promotional content. NO affiliate-style language.

OUTPUT FORMAT - Return ONLY valid JSON, no other text:
{
  "title": "SEO-optimized title under 60 chars",
  "slug": "url-friendly-slug",
  "meta_description": "Compelling meta description, 150-160 chars",
  "excerpt": "2-3 sentence excerpt for the post preview",
  "tags": ["tag1", "tag2", "tag3", "tag4", "tag5"],
  "category": "single primary category",
  "image_query": "2-3 word search query for a relevant featured image",
  "content_html": "<p>Intro paragraph...</p><h2>Heading</h2><p>...</p> ... full article in HTML",
  "faq": [
    {"q": "Question 1?", "a": "Answer 1."},
    {"q": "Question 2?", "a": "Answer 2."},
    {"q": "Question 3?", "a": "Answer 3."}
  ]
}
"""


class AIWriter:
    def __init__(self, api_key: str, model_name: str = "gemini-2.0-flash"):
        genai.configure(api_key=api_key)
        self.model = genai.GenerativeModel(
            model_name=model_name,
            system_instruction=SYSTEM_PROMPT,
        )

    def generate_post(self, topic: str, niche: str) -> dict:
        prompt = f"""Niche: {niche}
Topic: {topic}

Write a complete original blog post on this topic following all rules.
Include 3 FAQ items at the end. Return ONLY the JSON object."""

        response = self.model.generate_content(
            prompt,
            generation_config={
                "temperature": 0.8,
                "top_p": 0.95,
                "max_output_tokens": 8192,
                "response_mime_type": "application/json",
            },
        )

        text = response.text.strip()
        # Strip markdown code fences if Gemini adds them
        text = re.sub(r"^```(?:json)?\s*", "", text)
        text = re.sub(r"\s*```$", "", text)

        try:
            data = json.loads(text)
        except json.JSONDecodeError as e:
            raise ValueError(f"AI returned invalid JSON: {e}\n\nRaw output:\n{text[:500]}")

        # Append FAQ HTML to content
        if data.get("faq"):
            faq_html = "<h2>Frequently Asked Questions</h2>"
            for item in data["faq"]:
                faq_html += f"<h3>{item['q']}</h3><p>{item['a']}</p>"
            data["content_html"] = data.get("content_html", "") + faq_html

        return data
