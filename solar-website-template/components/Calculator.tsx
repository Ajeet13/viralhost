"use client";
import { useState, useMemo } from "react";
import { Calculator as CalcIcon, IndianRupee } from "lucide-react";

export default function Calculator() {
  const [bill, setBill] = useState(3000);
  const [units, setUnits] = useState(400);

  const result = useMemo(() => {
    const kwNeeded = Math.max(1, Math.round(units / 120));
    const systemCost = kwNeeded * 65000;
    const subsidy = Math.min(kwNeeded * 30000, 78000);
    const finalCost = systemCost - subsidy;
    const monthlySaving = bill * 0.9;
    const yearlySaving = monthlySaving * 12;
    const payback = Math.max(1, Math.round(finalCost / yearlySaving));
    const lifetimeSavings = yearlySaving * 25 - finalCost;
    return {
      kwNeeded,
      systemCost,
      subsidy,
      finalCost,
      monthlySaving,
      yearlySaving,
      payback,
      lifetimeSavings,
    };
  }, [bill, units]);

  return (
    <section id="calculator" className="section">
      <div className="container-x">
        <div className="text-center max-w-2xl mx-auto">
          <h2 className="text-3xl md:text-5xl font-bold text-gray-900">
            <span className="text-eco-600">बचत कैलकुलेटर</span>
          </h2>
          <p className="mt-4 text-gray-600">
            अपना बिजली बिल डालें और देखें कि सोलर लगवाने पर कितनी बचत होगी।
          </p>
        </div>

        <div className="mt-12 grid lg:grid-cols-2 gap-8 items-center">
          <div className="bg-white border border-gray-200 rounded-3xl p-6 md:p-8 shadow-lg">
            <div className="flex items-center gap-2 text-brand-700 font-bold text-lg">
              <CalcIcon className="h-5 w-5" /> अपनी जानकारी डालें
            </div>

            <div className="mt-6">
              <label className="font-semibold text-gray-800">
                मासिक बिजली बिल: ₹{bill.toLocaleString("en-IN")}
              </label>
              <input
                type="range"
                min="500"
                max="20000"
                step="100"
                value={bill}
                onChange={(e) => setBill(parseInt(e.target.value))}
                className="w-full mt-2 accent-brand-600"
              />
            </div>

            <div className="mt-6">
              <label className="font-semibold text-gray-800">
                मासिक यूनिट खपत: {units} units
              </label>
              <input
                type="range"
                min="50"
                max="3000"
                step="10"
                value={units}
                onChange={(e) => setUnits(parseInt(e.target.value))}
                className="w-full mt-2 accent-brand-600"
              />
            </div>

            <a href="#contact" className="btn-primary w-full mt-8">
              मुफ्त साइट विज़िट बुक करें
            </a>
          </div>

          <div className="bg-gradient-to-br from-eco-600 to-eco-700 text-white rounded-3xl p-6 md:p-8 shadow-2xl">
            <h3 className="text-2xl font-bold">आपकी अनुमानित बचत</h3>
            <div className="mt-6 grid grid-cols-2 gap-4">
              <Result label="सिस्टम साइज़" value={`${result.kwNeeded} kW`} />
              <Result
                label="सिस्टम कीमत"
                value={`₹${result.systemCost.toLocaleString("en-IN")}`}
              />
              <Result
                label="सरकारी सब्सिडी"
                value={`₹${result.subsidy.toLocaleString("en-IN")}`}
                highlight
              />
              <Result
                label="आपकी कीमत"
                value={`₹${result.finalCost.toLocaleString("en-IN")}`}
              />
              <Result
                label="मासिक बचत"
                value={`₹${Math.round(result.monthlySaving).toLocaleString("en-IN")}`}
              />
              <Result label="पेबैक" value={`${result.payback} साल`} />
            </div>
            <div className="mt-6 rounded-2xl bg-white/10 p-4 backdrop-blur">
              <div className="text-sm uppercase opacity-80">25 साल में कुल बचत</div>
              <div className="text-3xl md:text-4xl font-extrabold mt-1 flex items-center">
                <IndianRupee className="h-6 w-6" />
                {Math.round(result.lifetimeSavings).toLocaleString("en-IN")}
              </div>
            </div>
            <p className="text-xs opacity-80 mt-4">
              * अनुमानित गणना। सटीक कोटेशन के लिए हमारे एक्सपर्ट से मिलें।
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}

function Result({
  label,
  value,
  highlight = false,
}: {
  label: string;
  value: string;
  highlight?: boolean;
}) {
  return (
    <div
      className={`rounded-xl p-4 ${
        highlight ? "bg-yellow-300 text-gray-900" : "bg-white/10 backdrop-blur"
      }`}
    >
      <div className="text-xs uppercase opacity-80">{label}</div>
      <div className="text-lg font-bold mt-1">{value}</div>
    </div>
  );
}
