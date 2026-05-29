import type { Metadata } from "next";
import PlagiarismChecker from "@/components/plagiarism/PlagiarismChecker";

export const metadata: Metadata = {
  title: "Professional Plagiarism Checker — AI Detection & Deep Scan",
  description:
    "Free professional plagiarism checker with AI detection, fingerprint analysis, and auto-replacement. Supports Normal, Deep, and Academic scan modes.",
};

export default function PlagiarismCheckerPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <PlagiarismChecker />
    </div>
  );
}
