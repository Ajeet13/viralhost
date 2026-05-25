"""
Fetches royalty-free featured images from Pexels.
"""
import requests
from typing import Optional


class PexelsImageFetcher:
    def __init__(self, api_key: str):
        self.api_key = api_key
        self.headers = {"Authorization": api_key}

    def search_image(self, query: str) -> Optional[dict]:
        r = requests.get(
            "https://api.pexels.com/v1/search",
            headers=self.headers,
            params={"query": query, "per_page": 5, "orientation": "landscape"},
            timeout=30,
        )
        if not r.ok:
            return None
        data = r.json()
        if not data.get("photos"):
            return None
        photo = data["photos"][0]
        return {
            "url": photo["src"]["large"],
            "photographer": photo["photographer"],
            "alt": photo.get("alt") or query,
        }

    def download(self, url: str) -> bytes:
        r = requests.get(url, timeout=60)
        r.raise_for_status()
        return r.content
