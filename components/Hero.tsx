import { CheckCircle2, IndianRupee, ShieldCheck } from "lucide-react";
import { whatsappLink, WHATSAPP_MESSAGES, SITE_CONFIG } from "@/lib/config";

export default function Hero() {
  return (
    <section className="relative overflow-hidden bg-gradient-to-br from-brand-50 via-white to-eco-500/10">
      <div className="container-x grid md:grid-cols-2 gap-10 items-center py-16 md:py-24">
        <div>
          <span className="inline-block rounded-full bg-brand-100 text-brand-700 px-4 py-1 text-sm font-semibold">
            ⚡ PM Surya Ghar Yojana — ₹78,000 तक सब्सिडी
          </span>
          <h1 className="mt-5 text-4xl md:text-6xl font-extrabold leading-tight text-gray-900">
            अपनी छत को बनाएं{" "}
            <span className="text-brand-600">पावर हाउस</span>
            <br />
            <span className="text-eco-600">हर महीने ₹3000+ बचाएं</span>
          </h1>
          <p className="mt-5 text-lg text-gray-700 max-w-xl">
            भारत की भरोसेमंद सोलर इंस्टॉलेशन कंपनी। 25 साल वारंटी, सरकारी
            सब्सिडी की पूरी सहायता, और मुफ्त साइट विज़िट।
          </p>
          <div className="mt-8 flex flex-wrap gap-4">
            <a href="#contact" className="btn-primary">
              मुफ्त कोटेशन लें
            </a>
            <a
              href={whatsappLink(WHATSAPP_MESSAGES.general)}
              target="_blank"
              rel="noopener noreferrer"
              className="btn-whatsapp"
            >
              WhatsApp पर बात करें
            </a>
          </div>
          <div className="mt-8 grid grid-cols-3 gap-4 max-w-lg">
            <Stat value={SITE_CONFIG.customersServed} label="स्थापित सिस्टम" />
            <Stat value={`${SITE_CONFIG.yearsWarranty} साल`} label="पैनल वारंटी" />
            <Stat value={`${SITE_CONFIG.rating}★`} label="गूगल रेटिंग" />
          </div>
        </div>
        <div className="relative">
          <div className="aspect-[4/3] rounded-3xl bg-gradient-to-br from-brand-400 to-brand-700 p-1 shadow-2xl">
            <div className="h-full w-full rounded-3xl bg-white p-8 flex flex-col justify-between">
              <div>
                <h3 className="font-bold text-xl text-gray-900">
                  आज ही कोटेशन पाएं
                </h3>
                <p className="text-sm text-gray-600 mt-1">
                  24 घंटे में मुफ्त साइट विज़िट
                </p>
              </div>
              <ul className="space-y-3">
                <Feature icon={<IndianRupee className="text-eco-600" />} text="₹78,000 तक सब्सिडी की पूरी मदद" />
                <Feature icon={<ShieldCheck className="text-eco-600" />} text="25 साल पैनल + 10 साल इन्वर्टर वारंटी" />
                <Feature icon={<CheckCircle2 className="text-eco-600" />} text="MNRE अप्रूव्ड पैनल और इंस्टॉलेशन" />
              </ul>
              <a href="#calculator" className="btn-secondary w-full">
                बचत कैलकुलेटर देखें
              </a>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

function Stat({ value, label }: { value: string; label: string }) {
  return (
    <div>
      <div className="text-2xl md:text-3xl font-bold text-brand-700">{value}</div>
      <div className="text-sm text-gray-600">{label}</div>
    </div>
  );
}

function Feature({ icon, text }: { icon: React.ReactNode; text: string }) {
  return (
    <li className="flex items-start gap-3">
      <span className="mt-0.5">{icon}</span>
      <span className="text-gray-800">{text}</span>
    </li>
  );
}
