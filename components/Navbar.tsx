"use client";
import { useState } from "react";
import { Sun, Phone, Menu, X } from "lucide-react";
import { SITE_CONFIG } from "@/lib/config";

const links = [
  { href: "#services", label: "सेवाएं" },
  { href: "#calculator", label: "बचत कैलकुलेटर" },
  { href: "#subsidy", label: "सब्सिडी" },
  { href: "#testimonials", label: "ग्राहक" },
  { href: "#contact", label: "संपर्क" },
];

export default function Navbar() {
  const [open, setOpen] = useState(false);

  return (
    <header className="sticky top-0 z-50 border-b border-gray-100 bg-white/90 backdrop-blur">
      <div className="container-x flex h-16 items-center justify-between">
        <a href="#" className="flex items-center gap-2 font-bold text-xl">
          <Sun className="h-7 w-7 text-brand-500" />
          <span>{SITE_CONFIG.businessName}</span>
        </a>
        <nav className="hidden md:flex items-center gap-8">
          {links.map((l) => (
            <a
              key={l.href}
              href={l.href}
              className="text-gray-700 hover:text-brand-600 font-medium"
            >
              {l.label}
            </a>
          ))}
        </nav>
        <a
          href={`tel:${SITE_CONFIG.phoneNumber}`}
          className="hidden md:inline-flex btn-primary"
        >
          <Phone className="h-4 w-4" /> Call Now
        </a>
        <button
          aria-label="Toggle menu"
          className="md:hidden p-2"
          onClick={() => setOpen(!open)}
        >
          {open ? <X /> : <Menu />}
        </button>
      </div>
      {open && (
        <div className="md:hidden border-t border-gray-100 bg-white">
          <div className="container-x flex flex-col py-4 gap-3">
            {links.map((l) => (
              <a
                key={l.href}
                href={l.href}
                onClick={() => setOpen(false)}
                className="py-2 font-medium text-gray-700"
              >
                {l.label}
              </a>
            ))}
            <a href={`tel:${SITE_CONFIG.phoneNumber}`} className="btn-primary w-full">
              <Phone className="h-4 w-4" /> Call Now
            </a>
          </div>
        </div>
      )}
    </header>
  );
}
