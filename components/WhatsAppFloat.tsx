import { MessageCircle } from "lucide-react";
import { whatsappLink, WHATSAPP_MESSAGES } from "@/lib/config";

export default function WhatsAppFloat() {
  return (
    <a
      href={whatsappLink(WHATSAPP_MESSAGES.general)}
      target="_blank"
      rel="noopener noreferrer"
      aria-label="WhatsApp पर बात करें"
      className="fixed bottom-6 right-6 z-50 h-14 w-14 rounded-full bg-eco-600 hover:bg-eco-700 text-white shadow-2xl flex items-center justify-center transition hover:scale-110"
    >
      <MessageCircle className="h-7 w-7" />
      <span className="absolute -top-1 -right-1 h-4 w-4 bg-red-500 rounded-full animate-pulse" />
    </a>
  );
}
