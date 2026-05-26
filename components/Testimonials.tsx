import { Star, Quote } from "lucide-react";

const reviews = [
  {
    name: "राजेश कुमार",
    location: "जयपुर, राजस्थान",
    system: "5 kW सिस्टम",
    text: "बिल पहले ₹6000 आता था, अब ₹500 भी नहीं आता। सब्सिडी भी 25 दिन में मिल गई। बहुत बढ़िया सर्विस।",
    saving: "₹66,000/साल",
  },
  {
    name: "सुनीता शर्मा",
    location: "लखनऊ, उत्तर प्रदेश",
    system: "3 kW सिस्टम",
    text: "टीम बहुत प्रोफेशनल थी। पूरा कागजी काम उन्होंने किया, मुझे कुछ नहीं करना पड़ा। 10 दिन में सब हो गया।",
    saving: "₹42,000/साल",
  },
  {
    name: "अमित पटेल",
    location: "अहमदाबाद, गुजरात",
    system: "10 kW कमर्शियल",
    text: "मेरी फैक्ट्री के लिए लगवाया। ROI सिर्फ 3 साल में मिल गया। अब बिजली का बिल लगभग ज़ीरो।",
    saving: "₹1,80,000/साल",
  },
];

export default function Testimonials() {
  return (
    <section id="testimonials" className="section">
      <div className="container-x">
        <div className="text-center max-w-2xl mx-auto">
          <h2 className="text-3xl md:text-5xl font-bold text-gray-900">
            हमारे <span className="text-brand-600">5000+ खुश ग्राहक</span>
          </h2>
          <div className="mt-4 flex items-center justify-center gap-2">
            {[...Array(5)].map((_, i) => (
              <Star key={i} className="h-5 w-5 fill-yellow-400 text-yellow-400" />
            ))}
            <span className="ml-2 font-bold text-gray-900">4.9/5</span>
            <span className="text-gray-600">(2,400+ reviews)</span>
          </div>
        </div>

        <div className="mt-12 grid md:grid-cols-3 gap-6">
          {reviews.map((r) => (
            <div
              key={r.name}
              className="rounded-2xl bg-white border border-gray-100 p-6 shadow-sm hover:shadow-xl transition"
            >
              <Quote className="h-8 w-8 text-brand-400" />
              <p className="mt-4 text-gray-700 italic">"{r.text}"</p>
              <div className="mt-6 pt-4 border-t border-gray-100">
                <div className="font-bold text-gray-900">{r.name}</div>
                <div className="text-sm text-gray-600">{r.location}</div>
                <div className="mt-2 flex items-center justify-between">
                  <span className="text-xs bg-brand-50 text-brand-700 px-2 py-1 rounded-full font-semibold">
                    {r.system}
                  </span>
                  <span className="text-xs bg-eco-500/10 text-eco-700 px-2 py-1 rounded-full font-semibold">
                    बचत: {r.saving}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
