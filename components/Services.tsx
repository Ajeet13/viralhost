import { Home, Building2, Tractor, Wrench, FileText, Battery } from "lucide-react";

const services = [
  {
    icon: Home,
    title: "घरेलू रूफटॉप सोलर",
    desc: "1 kW से 10 kW तक के सिस्टम। बिजली बिल 90% तक कम करें।",
    price: "₹65,000 से शुरू",
  },
  {
    icon: Building2,
    title: "कमर्शियल सोलर",
    desc: "दुकान, ऑफिस, फैक्ट्री के लिए 10 kW से 1 MW तक।",
    price: "Custom Quote",
  },
  {
    icon: Tractor,
    title: "एग्रीकल्चर सोलर पंप",
    desc: "PM-KUSUM योजना के तहत किसानों के लिए सोलर पंप।",
    price: "60% सब्सिडी",
  },
  {
    icon: Battery,
    title: "हाइब्रिड सिस्टम + बैटरी",
    desc: "बिजली कटौती के बावजूद 24/7 बिजली। बैटरी बैकअप के साथ।",
    price: "₹1,20,000 से",
  },
  {
    icon: Wrench,
    title: "AMC और मरम्मत",
    desc: "सालाना रखरखाव, सफाई, मॉनिटरिंग। पुराने सिस्टम भी रिपेयर।",
    price: "₹3,000/साल",
  },
  {
    icon: FileText,
    title: "सब्सिडी और कागजी काम",
    desc: "PM Surya Ghar पोर्टल पर पूरा काम। नेट मीटरिंग की मदद।",
    price: "Free with Install",
  },
];

export default function Services() {
  return (
    <section id="services" className="section bg-gray-50">
      <div className="container-x">
        <div className="text-center max-w-2xl mx-auto">
          <h2 className="text-3xl md:text-5xl font-bold text-gray-900">
            हमारी <span className="text-brand-600">सेवाएं</span>
          </h2>
          <p className="mt-4 text-gray-600">
            सोलर पैनल इंस्टॉलेशन से लेकर सब्सिडी पेपर वर्क तक — सब कुछ एक छत के नीचे।
          </p>
        </div>
        <div className="mt-12 grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {services.map((s) => (
            <div
              key={s.title}
              className="group rounded-2xl border border-gray-100 bg-white p-6 hover:border-brand-400 hover:shadow-xl transition"
            >
              <div className="h-12 w-12 rounded-xl bg-brand-50 text-brand-600 flex items-center justify-center group-hover:bg-brand-600 group-hover:text-white transition">
                <s.icon className="h-6 w-6" />
              </div>
              <h3 className="mt-4 text-xl font-bold text-gray-900">{s.title}</h3>
              <p className="mt-2 text-gray-600">{s.desc}</p>
              <p className="mt-4 font-semibold text-eco-700">{s.price}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
