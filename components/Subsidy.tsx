import { Award, FileCheck, Banknote, Clock } from "lucide-react";

const steps = [
  {
    icon: FileCheck,
    title: "1. रजिस्ट्रेशन",
    desc: "PM Surya Ghar पोर्टल पर हम आपका रजिस्ट्रेशन करते हैं।",
  },
  {
    icon: Clock,
    title: "2. सर्वे और इंस्टॉलेशन",
    desc: "हमारी टीम साइट विज़िट करके 7-15 दिन में सोलर लगाती है।",
  },
  {
    icon: Award,
    title: "3. नेट मीटरिंग",
    desc: "बिजली विभाग से नेट मीटर लगवाने का काम भी हम करते हैं।",
  },
  {
    icon: Banknote,
    title: "4. सब्सिडी सीधे खाते में",
    desc: "30 दिन में ₹78,000 तक सब्सिडी आपके बैंक खाते में आ जाती है।",
  },
];

export default function Subsidy() {
  return (
    <section id="subsidy" className="section bg-gradient-to-br from-brand-700 to-brand-600 text-white">
      <div className="container-x">
        <div className="text-center max-w-3xl mx-auto">
          <span className="inline-block rounded-full bg-white/20 px-4 py-1 text-sm font-semibold">
            🇮🇳 भारत सरकार की योजना
          </span>
          <h2 className="text-3xl md:text-5xl font-bold mt-4">
            PM Surya Ghar Yojana — ₹78,000 तक सब्सिडी
          </h2>
          <p className="mt-4 text-white/90 text-lg">
            हम आपके लिए A से Z तक सब काम करते हैं — रजिस्ट्रेशन, इंस्टॉलेशन,
            नेट मीटरिंग और सब्सिडी क्लेम।
          </p>
        </div>

        <div className="mt-12 grid md:grid-cols-4 gap-4">
          {steps.map((s) => (
            <div
              key={s.title}
              className="rounded-2xl bg-white/10 backdrop-blur p-6 hover:bg-white/15 transition"
            >
              <div className="h-12 w-12 rounded-xl bg-white text-brand-700 flex items-center justify-center">
                <s.icon className="h-6 w-6" />
              </div>
              <h3 className="mt-4 text-xl font-bold">{s.title}</h3>
              <p className="mt-2 text-white/90 text-sm">{s.desc}</p>
            </div>
          ))}
        </div>

        <div className="mt-12 rounded-3xl bg-white text-gray-900 p-6 md:p-10 grid md:grid-cols-3 gap-6 items-center">
          <div className="md:col-span-2">
            <h3 className="text-2xl font-bold">
              सब्सिडी का स्ट्रक्चर (Updated 2026)
            </h3>
            <ul className="mt-4 space-y-2 text-gray-700">
              <li>✓ <b>1 kW सिस्टम</b> — ₹30,000 सब्सिडी</li>
              <li>✓ <b>2 kW सिस्टम</b> — ₹60,000 सब्सिडी</li>
              <li>✓ <b>3 kW और ऊपर</b> — ₹78,000 सब्सिडी (अधिकतम)</li>
            </ul>
          </div>
          <a href="#contact" className="btn-primary justify-self-center md:justify-self-end">
            अभी आवेदन करें
          </a>
        </div>
      </div>
    </section>
  );
}
