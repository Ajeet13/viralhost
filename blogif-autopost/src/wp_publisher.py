"""
WordPress REST API publisher.
Handles auth, category/tag creation, media upload, post creation.
"""
import requests
from requests.auth import HTTPBasicAuth
from typing import Optional


class WordPressPublisher:
    def __init__(self, site_url: str, username: str, app_password: str):
        self.base = site_url.rstrip("/") + "/wp-json/wp/v2"
        # WP application passwords contain spaces - keep them, requests handles it
        self.auth = HTTPBasicAuth(username, app_password)
        self.session = requests.Session()
        self.session.auth = self.auth
        self.session.headers.update({"User-Agent": "blogif-autopost/1.0"})

    def _request(self, method: str, path: str, **kwargs):
        url = f"{self.base}{path}"
        r = self.session.request(method, url, timeout=60, **kwargs)
        if not r.ok:
            raise RuntimeError(f"WP {method} {path} failed [{r.status_code}]: {r.text[:300]}")
        return r.json()

    def test_connection(self) -> dict:
        r = self.session.get(f"{self.base}/users/me", timeout=30)
        if not r.ok:
            raise RuntimeError(f"WP auth failed [{r.status_code}]: {r.text[:300]}")
        return r.json()

    def get_or_create_category(self, name: str) -> int:
        existing = self._request("GET", "/categories", params={"search": name, "per_page": 100})
        for cat in existing:
            if cat["name"].lower() == name.lower():
                return cat["id"]
        created = self._request("POST", "/categories", json={"name": name})
        return created["id"]

    def get_or_create_tags(self, names: list) -> list:
        ids = []
        for name in names:
            name = name.strip()
            if not name:
                continue
            existing = self._request("GET", "/tags", params={"search": name, "per_page": 100})
            match = next((t for t in existing if t["name"].lower() == name.lower()), None)
            if match:
                ids.append(match["id"])
            else:
                created = self._request("POST", "/tags", json={"name": name})
                ids.append(created["id"])
        return ids

    def upload_media(self, image_bytes: bytes, filename: str, alt_text: str = "") -> int:
        headers = {
            "Content-Disposition": f'attachment; filename="{filename}"',
            "Content-Type": "image/jpeg",
        }
        r = self.session.post(
            f"{self.base}/media",
            data=image_bytes,
            headers=headers,
            timeout=120,
        )
        if not r.ok:
            raise RuntimeError(f"Media upload failed [{r.status_code}]: {r.text[:300]}")
        media = r.json()
        if alt_text:
            self._request("POST", f"/media/{media['id']}", json={"alt_text": alt_text})
        return media["id"]

    def create_post(
        self,
        title: str,
        content_html: str,
        slug: str,
        excerpt: str,
        meta_description: str,
        category_ids: list,
        tag_ids: list,
        featured_media_id: Optional[int] = None,
        status: str = "draft",
        author_id: int = 1,
    ) -> dict:
        payload = {
            "title": title,
            "content": content_html,
            "slug": slug,
            "excerpt": excerpt,
            "status": status,
            "author": author_id,
            "categories": category_ids,
            "tags": tag_ids,
            # Yoast / Rank Math / generic meta - many SEO plugins read this
            "meta": {
                "_yoast_wpseo_metadesc": meta_description,
                "rank_math_description": meta_description,
            },
        }
        if featured_media_id:
            payload["featured_media"] = featured_media_id
        return self._request("POST", "/posts", json=payload)

    def create_page(self, title: str, content_html: str, slug: str, status: str = "publish") -> dict:
        return self._request("POST", "/pages", json={
            "title": title,
            "content": content_html,
            "slug": slug,
            "status": status,
        })
