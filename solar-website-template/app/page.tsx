import Navbar from "@/components/Navbar";
import Hero from "@/components/Hero";
import Services from "@/components/Services";
import Calculator from "@/components/Calculator";
import Subsidy from "@/components/Subsidy";
import Testimonials from "@/components/Testimonials";
import ContactForm from "@/components/ContactForm";
import Footer from "@/components/Footer";
import WhatsAppFloat from "@/components/WhatsAppFloat";

export default function Home() {
  return (
    <>
      <Navbar />
      <main>
        <Hero />
        <Services />
        <Calculator />
        <Subsidy />
        <Testimonials />
        <ContactForm />
      </main>
      <Footer />
      <WhatsAppFloat />
    </>
  );
}
