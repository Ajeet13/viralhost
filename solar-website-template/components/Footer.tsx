import { Sun, Phone, Mail, MapPin } from "lucide-react";
import { SITE_CONFIG } from "@/lib/config";

export default function Footer() {
  return (
    <footer className="bg-gray-900 text-gray-300">
      <div className="container-x py-12 grid md:grid-cols-4 gap-8">
        <div>
          <div className="flex items-center gap-2 text-white font-bold text-xl">
            <Sun className="h-7 w-7 text-brand-400" />
            {SITE_CONFIG.businessName}
          </div>
          <p className="mt-3 text-sm">
            भारत की भरोसेमंद सोलर इंस्टॉलेशन कंपनी। MNRE अप्रूव्ड चैनल पार्टनर।
          </p>
        </div>
        <div>
          <h4 className="text-white font-bold mb-3">सेवाएं</h4>
          <ul className="space-y-2 text-sm">
            <li><a href="#services" className="hover:text-brand-400">रूफटॉप सोलर</a></li>
            <li><a href="#services" className="hover:text-brand-400">कमर्शियल सोलर</a></li>
            <li><a href="#services" className="hover:text-brand-400">सोलर पंप</a></li>
            <li><a href="#services" className="hover:text-brand-400">AMC</a></li>
          </ul>
        </div>
        <div>
          <h4 className="text-white font-bold mb-3">कंपनी</h4>
          <ul className="space-y-2 text-sm">
            <li><a href="#" className="hover:text-brand-400">हमारे बारे में</a></li>
            <li><a href="#testimonials" className="hover:text-brand-400">ग्राहक</a></li>
            <li><a href="#subsidy" className="hover:text-brand-400">सब्सिडी</a></li>
            <li><a href="#contact" className="hover:text-brand-400">संपर्क</a></li>
          </ul>
        </div>
        <div>
          <h4 className="text-white font-bold mb-3">संपर्क</h4>
          <ul className="space-y-2 text-sm">
            <li className="flex items-center gap-2">
              <Phone className="h-4 w-4" />
              <a href={`tel:${SITE_CONFIG.phoneNumber}`}>{SITE_CONFIG.phoneNumber}</a>
            </li>
            <li className="flex items-center gap-2">
              <Mail className="h-4 w-4" />
              <a href={`mailto:${SITE_CONFIG.email}`}>{SITE_CONFIG.email}</a>
            </li>
            <li className="flex items-start gap-2">
              <MapPin className="h-4 w-4 mt-1" /> {SITE_CONFIG.fullAddress}
            </li>
          </ul>
        </div>
      </div>
      <div className="border-t border-gray-800">
        <div className="container-x py-6 text-sm flex flex-col md:flex-row justify-between gap-2">
          <div>© 2026 {SITE_CONFIG.businessName}. सर्वाधिकार सुरक्षित।</div>
          <div className="flex gap-6">
            <a href="#" className="hover:text-brand-400">Privacy Policy</a>
            <a href="#" className="hover:text-brand-400">Terms</a>
          </div>
        </div>
      </div>
    </footer>
  );
}
