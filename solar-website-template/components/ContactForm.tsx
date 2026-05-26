"use client";
import { useState } from "react";
import { Phone, Mail, MapPin, Send, CheckCircle2, MessageCircle } from "lucide-react";
import {
  SITE_CONFIG,
  whatsappLink,
  formatLeadMessage,
} from "@/lib/config";

export default function ContactForm() {
  const [submitted, setSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [form, setForm] = useState({
    name: "",
    phone: "",
    city: "",
    bill: "",
    message: "",
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      // 1. Save to backend (email/sheets)
      const res = await fetch("/api/lead", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...form, source: "contact-form" }),
      });

      const data = await res.json();

      if (!res.ok) {
        setError(data.error || "Something went wrong");
        setLoading(false);
        return;
      }

      // 2. ALSO open WhatsApp (instant alert to business)
      const msg = formatLeadMessage(form);
      window.open(whatsappLink(msg), "_blank");

      setSubmitted(true);
    } catch (err) {
      setError("Connection issue. कृपया WhatsApp पर संपर्क करें।");
    }
    setLoading(false);
  };

  return (
    <section id="contact" className="section bg-gray-50">
      <div className="container-x">
        <div className="grid lg:grid-cols-2 gap-10">
          <div>
            <h2 className="text-3xl md:text-5xl font-bold text-gray-900">
              मुफ्त <span className="text-brand-600">कोटेशन पाएं</span>
            </h2>
            <p className="mt-4 text-gray-700 text-lg">
              फॉर्म भरें या सीधे कॉल/WhatsApp करें। हम 24 घंटे में संपर्क करेंगे।
            </p>

            <div className="mt-8 space-y-5">
              <ContactItem
                icon={<Phone />}
                label="फोन"
                value={SITE_CONFIG.phoneNumber}
                href={`tel:${SITE_CONFIG.phoneNumber}`}
              />
              <ContactItem
                icon={<Mail />}
                label="ईमेल"
                value={SITE_CONFIG.email}
                href={`mailto:${SITE_CONFIG.email}`}
              />
              <ContactItem
                icon={<MapPin />}
                label="ऑफिस"
                value={SITE_CONFIG.fullAddress}
              />
            </div>

            <div className="mt-8 p-6 rounded-2xl bg-eco-500/10 border border-eco-500/30">
              <div className="font-bold text-eco-700 flex items-center gap-2">
                <MessageCircle className="h-5 w-5" /> तुरंत सहायता चाहिए?
              </div>
              <p className="text-gray-700 mt-1">
                WhatsApp पर "Hi" भेजें — हमारा एक्सपर्ट तुरंत बात करेगा।
              </p>
              <a
                href={whatsappLink("Hi, मुझे सोलर के बारे में जानकारी चाहिए")}
                target="_blank"
                rel="noopener noreferrer"
                className="btn-whatsapp mt-4"
              >
                <MessageCircle className="h-4 w-4" /> WhatsApp चैट शुरू करें
              </a>
            </div>
          </div>

          <div className="bg-white rounded-3xl p-6 md:p-8 shadow-xl border border-gray-100">
            {submitted ? (
              <div className="text-center py-12">
                <CheckCircle2 className="h-16 w-16 text-eco-600 mx-auto" />
                <h3 className="text-2xl font-bold mt-4 text-gray-900">
                  धन्यवाद! आपका संदेश मिल गया।
                </h3>
                <p className="text-gray-600 mt-2">
                  हम 24 घंटे में आपसे संपर्क करेंगे।
                </p>
                <a
                  href={whatsappLink("Hi")}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn-whatsapp mt-6"
                >
                  <MessageCircle className="h-4 w-4" /> WhatsApp पर तुरंत बात करें
                </a>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-4">
                <Input
                  label="आपका नाम *"
                  value={form.name}
                  onChange={(v) => setForm({ ...form, name: v })}
                  required
                />
                <Input
                  label="मोबाइल नंबर *"
                  type="tel"
                  value={form.phone}
                  onChange={(v) => setForm({ ...form, phone: v })}
                  required
                  pattern="[6-9]{1}[0-9]{9}"
                />
                <Input
                  label="शहर *"
                  value={form.city}
                  onChange={(v) => setForm({ ...form, city: v })}
                  required
                />
                <Input
                  label="मासिक बिजली बिल (₹)"
                  type="number"
                  value={form.bill}
                  onChange={(v) => setForm({ ...form, bill: v })}
                />
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    संदेश
                  </label>
                  <textarea
                    rows={3}
                    value={form.message}
                    onChange={(e) =>
                      setForm({ ...form, message: e.target.value })
                    }
                    className="mt-1 block w-full rounded-xl border-gray-300 shadow-sm focus:border-brand-500 focus:ring-brand-500 p-3 border"
                    placeholder="कोई विशेष आवश्यकता?"
                  />
                </div>

                {error && (
                  <div className="p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm">
                    ⚠ {error}
                  </div>
                )}

                <button
                  type="submit"
                  disabled={loading}
                  className="btn-primary w-full disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  {loading ? (
                    "भेजा जा रहा है..."
                  ) : (
                    <>
                      <Send className="h-4 w-4" /> कोटेशन रिक्वेस्ट भेजें
                    </>
                  )}
                </button>
                <p className="text-xs text-gray-500 text-center">
                  आपकी जानकारी सुरक्षित है। हम स्पैम नहीं करते।
                </p>
              </form>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}

function Input({
  label,
  type = "text",
  value,
  onChange,
  required = false,
  pattern,
}: {
  label: string;
  type?: string;
  value: string;
  onChange: (v: string) => void;
  required?: boolean;
  pattern?: string;
}) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700">{label}</label>
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required={required}
        pattern={pattern}
        className="mt-1 block w-full rounded-xl border-gray-300 shadow-sm focus:border-brand-500 focus:ring-brand-500 p-3 border"
      />
    </div>
  );
}

function ContactItem({
  icon,
  label,
  value,
  href,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
  href?: string;
}) {
  const content = (
    <div className="flex items-start gap-3">
      <div className="h-10 w-10 rounded-xl bg-brand-50 text-brand-600 flex items-center justify-center">
        {icon}
      </div>
      <div>
        <div className="text-sm text-gray-500">{label}</div>
        <div className="font-semibold text-gray-900">{value}</div>
      </div>
    </div>
  );
  return href ? <a href={href}>{content}</a> : content;
}
