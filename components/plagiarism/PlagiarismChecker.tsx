"use client";
import { useState, useCallback } from "react";
import {
  Shield,
  Search,
  FileText,
  Brain,
  Fingerprint,
  RefreshCw,
  CheckCircle,
  AlertTriangle,
  XCircle,
  Zap,
  BookOpen,
  Quote,
  ToggleLeft,
  ToggleRight,
  Copy,
  Download,
} from "lucide-react";

type ScanMode = "normal" | "deep" | "academic";
type ScanStatus = "idle" | "scanning" | "complete";

interface PlagiarismResult {
  originalText: string;
  matchedSource: string;
  sourceUrl: string;
  similarity: number;
  replacement: string;
}

interface AIDetectionResult {
  aiProbability: number;
  humanProbability: number;
  fingerprint: string;
  patterns: string[];
}


interface ScanResults {
  overallScore: number;
  uniqueContent: number;
  plagiarizedSentences: PlagiarismResult[];
  aiDetection: AIDetectionResult;
  sourcesFound: number;
  wordsScanned: number;
  scanTime: number;
}

export default function PlagiarismChecker() {
  const [text, setText] = useState("");
  const [scanMode, setScanMode] = useState<ScanMode>("normal");
  const [status, setStatus] = useState<ScanStatus>("idle");
  const [progress, setProgress] = useState(0);
  const [results, setResults] = useState<ScanResults | null>(null);
  const [excludeQuotes, setExcludeQuotes] = useState(false);
  const [excludeBibliography, setExcludeBibliography] = useState(false);
  const [showAIDetection, setShowAIDetection] = useState(true);
  const [showFingerprint, setShowFingerprint] = useState(false);
  const [activeTab, setActiveTab] = useState<"results" | "ai" | "replaced">("results");
  const [replacedText, setReplacedText] = useState("");


  const handleScan = useCallback(async () => {
    if (!text.trim() || text.trim().split(/\s+/).length < 20) return;
    setStatus("scanning");
    setProgress(0);
    setResults(null);
    setReplacedText("");

    // Simulate progressive scanning
    const interval = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 95) {
          clearInterval(interval);
          return 95;
        }
        return prev + Math.random() * 8;
      });
    }, 200);

    try {
      const res = await fetch("/api/plagiarism", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          text,
          scanMode,
          excludeQuotes,
          excludeBibliography,
          detectAI: showAIDetection,
        }),
      });
      const data: ScanResults = await res.json();
      clearInterval(interval);
      setProgress(100);
      setResults(data);
      setStatus("complete");

      // Build replaced text
      let newText = text;
      for (const item of data.plagiarizedSentences) {
        newText = newText.replace(item.originalText, item.replacement);
      }
      setReplacedText(newText);
    } catch {
      clearInterval(interval);
      setStatus("idle");
      setProgress(0);
    }
  }, [text, scanMode, excludeQuotes, excludeBibliography, showAIDetection]);


  const getScoreColor = (score: number) => {
    if (score >= 80) return "text-green-600";
    if (score >= 50) return "text-yellow-600";
    return "text-red-600";
  };

  const getScoreBg = (score: number) => {
    if (score >= 80) return "bg-green-100 border-green-300";
    if (score >= 50) return "bg-yellow-100 border-yellow-300";
    return "bg-red-100 border-red-300";
  };

  const getScoreIcon = (score: number) => {
    if (score >= 80) return <CheckCircle className="h-8 w-8 text-green-600" />;
    if (score >= 50) return <AlertTriangle className="h-8 w-8 text-yellow-600" />;
    return <XCircle className="h-8 w-8 text-red-600" />;
  };

  const wordCount = text.trim() ? text.trim().split(/\s+/).length : 0;

  const copyToClipboard = (content: string) => {
    navigator.clipboard.writeText(content);
  };


  return (
    <div className="min-h-screen">
      {/* Header */}
      <header className="bg-white border-b border-gray-200 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-3">
              <Shield className="h-8 w-8 text-indigo-600" />
              <div>
                <h1 className="text-lg font-bold text-gray-900">Plagiarism Checker Pro</h1>
                <p className="text-xs text-gray-500">AI-Powered Content Integrity Scanner</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <span className="hidden sm:inline-flex items-center gap-1 text-xs bg-indigo-100 text-indigo-700 px-2 py-1 rounded-full font-medium">
                <Zap className="h-3 w-3" /> PRO Features Active
              </span>
            </div>
          </div>
        </div>
      </header>


      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          {/* Left Sidebar - Settings */}
          <div className="lg:col-span-1 space-y-4">
            {/* Scan Mode */}
            <div className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
              <h3 className="text-sm font-semibold text-gray-900 mb-3 flex items-center gap-2">
                <Search className="h-4 w-4 text-indigo-600" /> Scan Mode
              </h3>
              <div className="space-y-2">
                <button
                  onClick={() => setScanMode("normal")}
                  className={`w-full text-left px-3 py-2 rounded-lg text-sm transition ${
                    scanMode === "normal"
                      ? "bg-indigo-100 text-indigo-800 font-medium"
                      : "hover:bg-gray-50 text-gray-700"
                  }`}
                >
                  <div className="font-medium">Normal Scan</div>
                  <div className="text-xs text-gray-500">Basic web comparison</div>
                </button>
                <button
                  onClick={() => setScanMode("deep")}
                  className={`w-full text-left px-3 py-2 rounded-lg text-sm transition ${
                    scanMode === "deep"
                      ? "bg-indigo-100 text-indigo-800 font-medium"
                      : "hover:bg-gray-50 text-gray-700"
                  }`}
                >
                  <div className="font-medium flex items-center gap-1">
                    Deep Scan <span className="text-xs bg-amber-100 text-amber-700 px-1.5 py-0.5 rounded">PRO</span>
                  </div>
                  <div className="text-xs text-gray-500">16B+ web pages & databases</div>
                </button>
                <button
                  onClick={() => setScanMode("academic")}
                  className={`w-full text-left px-3 py-2 rounded-lg text-sm transition ${
                    scanMode === "academic"
                      ? "bg-indigo-100 text-indigo-800 font-medium"
                      : "hover:bg-gray-50 text-gray-700"
                  }`}
                >
                  <div className="font-medium flex items-center gap-1">
                    Academic Scan <span className="text-xs bg-amber-100 text-amber-700 px-1.5 py-0.5 rounded">PRO</span>
                  </div>
                  <div className="text-xs text-gray-500">Journals, theses & papers</div>
                </button>
              </div>
            </div>


            {/* Exclusions */}
            <div className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
              <h3 className="text-sm font-semibold text-gray-900 mb-3 flex items-center gap-2">
                <FileText className="h-4 w-4 text-indigo-600" /> Exclusions
              </h3>
              <div className="space-y-3">
                <button
                  onClick={() => setExcludeQuotes(!excludeQuotes)}
                  className="w-full flex items-center justify-between"
                >
                  <span className="text-sm text-gray-700 flex items-center gap-2">
                    <Quote className="h-3.5 w-3.5" /> Exclude Quotes
                    <span className="text-xs bg-amber-100 text-amber-700 px-1.5 py-0.5 rounded">PRO</span>
                  </span>
                  {excludeQuotes ? (
                    <ToggleRight className="h-6 w-6 text-indigo-600" />
                  ) : (
                    <ToggleLeft className="h-6 w-6 text-gray-400" />
                  )}
                </button>
                <button
                  onClick={() => setExcludeBibliography(!excludeBibliography)}
                  className="w-full flex items-center justify-between"
                >
                  <span className="text-sm text-gray-700 flex items-center gap-2">
                    <BookOpen className="h-3.5 w-3.5" /> Exclude Bibliography
                    <span className="text-xs bg-amber-100 text-amber-700 px-1.5 py-0.5 rounded">PRO</span>
                  </span>
                  {excludeBibliography ? (
                    <ToggleRight className="h-6 w-6 text-indigo-600" />
                  ) : (
                    <ToggleLeft className="h-6 w-6 text-gray-400" />
                  )}
                </button>
              </div>
            </div>


            {/* AI & Integrity */}
            <div className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
              <h3 className="text-sm font-semibold text-gray-900 mb-3 flex items-center gap-2">
                <Brain className="h-4 w-4 text-indigo-600" /> AI & Integrity
              </h3>
              <div className="space-y-3">
                <button
                  onClick={() => setShowAIDetection(!showAIDetection)}
                  className="w-full flex items-center justify-between"
                >
                  <span className="text-sm text-gray-700 flex items-center gap-2">
                    <Brain className="h-3.5 w-3.5" /> AI Detection
                  </span>
                  {showAIDetection ? (
                    <ToggleRight className="h-6 w-6 text-indigo-600" />
                  ) : (
                    <ToggleLeft className="h-6 w-6 text-gray-400" />
                  )}
                </button>
                <button
                  onClick={() => setShowFingerprint(!showFingerprint)}
                  className="w-full flex items-center justify-between"
                >
                  <span className="text-sm text-gray-700 flex items-center gap-2">
                    <Fingerprint className="h-3.5 w-3.5" /> Fingerprint
                    <span className="text-xs bg-amber-100 text-amber-700 px-1.5 py-0.5 rounded">PRO</span>
                  </span>
                  {showFingerprint ? (
                    <ToggleRight className="h-6 w-6 text-indigo-600" />
                  ) : (
                    <ToggleLeft className="h-6 w-6 text-gray-400" />
                  )}
                </button>
              </div>
            </div>
          </div>


          {/* Main Content */}
          <div className="lg:col-span-3 space-y-4">
            {/* Text Input Area */}
            <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
              <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100 bg-gray-50">
                <span className="text-sm font-medium text-gray-700">
                  Paste your content below
                </span>
                <span className="text-xs text-gray-500">
                  {wordCount} words {wordCount < 20 && wordCount > 0 && "(min 20 words)"}
                </span>
              </div>
              <textarea
                value={text}
                onChange={(e) => setText(e.target.value)}
                placeholder="Paste or type your text here to check for plagiarism. Minimum 20 words required for accurate scanning..."
                className="w-full h-64 p-4 resize-none focus:outline-none text-sm text-gray-800 leading-relaxed"
              />
              <div className="flex items-center justify-between px-4 py-3 border-t border-gray-100 bg-gray-50">
                <div className="flex items-center gap-3">
                  <span className="text-xs text-gray-500">
                    Mode: <strong className="capitalize">{scanMode}</strong>
                  </span>
                  {excludeQuotes && (
                    <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full">−Quotes</span>
                  )}
                  {excludeBibliography && (
                    <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full">−Bibliography</span>
                  )}
                </div>
                <button
                  onClick={handleScan}
                  disabled={status === "scanning" || wordCount < 20}
                  className={`inline-flex items-center gap-2 px-6 py-2.5 rounded-lg font-semibold text-sm transition shadow-md ${
                    status === "scanning" || wordCount < 20
                      ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                      : "bg-indigo-600 text-white hover:bg-indigo-700 shadow-indigo-600/30"
                  }`}
                >
                  {status === "scanning" ? (
                    <>
                      <RefreshCw className="h-4 w-4 animate-spin" /> Scanning...
                    </>
                  ) : (
                    <>
                      <Search className="h-4 w-4" /> Check Plagiarism
                    </>
                  )}
                </button>
              </div>
            </div>


            {/* Progress Bar */}
            {status === "scanning" && (
              <div className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm font-medium text-gray-700">
                    Scanning across the web...
                  </span>
                  <span className="text-sm text-indigo-600 font-medium">
                    {Math.round(progress)}%
                  </span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2.5">
                  <div
                    className="bg-indigo-600 h-2.5 rounded-full transition-all duration-300"
                    style={{ width: `${progress}%` }}
                  />
                </div>
                <div className="mt-2 flex items-center gap-4 text-xs text-gray-500">
                  <span>🔍 Checking 16B+ web pages</span>
                  <span>📚 Academic databases</span>
                  <span>🤖 AI pattern analysis</span>
                </div>
              </div>
            )}


            {/* Results Section */}
            {results && status === "complete" && (
              <>
                {/* Score Overview */}
                <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
                  <div className={`rounded-xl border p-4 shadow-sm ${getScoreBg(results.uniqueContent)}`}>
                    <div className="flex items-center gap-3">
                      {getScoreIcon(results.uniqueContent)}
                      <div>
                        <div className={`text-2xl font-bold ${getScoreColor(results.uniqueContent)}`}>
                          {results.uniqueContent}%
                        </div>
                        <div className="text-xs text-gray-600">Unique Content</div>
                      </div>
                    </div>
                  </div>
                  <div className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
                    <div className="text-2xl font-bold text-red-600">{results.overallScore}%</div>
                    <div className="text-xs text-gray-600">Plagiarized</div>
                  </div>
                  <div className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
                    <div className="text-2xl font-bold text-gray-900">{results.sourcesFound}</div>
                    <div className="text-xs text-gray-600">Sources Found</div>
                  </div>
                  <div className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
                    <div className="text-2xl font-bold text-gray-900">{results.scanTime}s</div>
                    <div className="text-xs text-gray-600">Scan Time</div>
                  </div>
                </div>


                {/* Tabs */}
                <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
                  <div className="flex border-b border-gray-200">
                    <button
                      onClick={() => setActiveTab("results")}
                      className={`flex-1 px-4 py-3 text-sm font-medium transition ${
                        activeTab === "results"
                          ? "border-b-2 border-indigo-600 text-indigo-700 bg-indigo-50"
                          : "text-gray-600 hover:text-gray-800"
                      }`}
                    >
                      <div className="flex items-center justify-center gap-2">
                        <FileText className="h-4 w-4" /> Plagiarism Results
                      </div>
                    </button>
                    <button
                      onClick={() => setActiveTab("ai")}
                      className={`flex-1 px-4 py-3 text-sm font-medium transition ${
                        activeTab === "ai"
                          ? "border-b-2 border-indigo-600 text-indigo-700 bg-indigo-50"
                          : "text-gray-600 hover:text-gray-800"
                      }`}
                    >
                      <div className="flex items-center justify-center gap-2">
                        <Brain className="h-4 w-4" /> AI Detection
                      </div>
                    </button>
                    <button
                      onClick={() => setActiveTab("replaced")}
                      className={`flex-1 px-4 py-3 text-sm font-medium transition ${
                        activeTab === "replaced"
                          ? "border-b-2 border-indigo-600 text-indigo-700 bg-indigo-50"
                          : "text-gray-600 hover:text-gray-800"
                      }`}
                    >
                      <div className="flex items-center justify-center gap-2">
                        <RefreshCw className="h-4 w-4" /> Rewritten Text
                      </div>
                    </button>
                  </div>


                  {/* Plagiarism Results Tab */}
                  {activeTab === "results" && (
                    <div className="p-4 space-y-3">
                      {results.plagiarizedSentences.length === 0 ? (
                        <div className="text-center py-8">
                          <CheckCircle className="h-12 w-12 text-green-500 mx-auto mb-3" />
                          <p className="text-green-700 font-medium">No plagiarism detected!</p>
                          <p className="text-sm text-gray-500">Your content is 100% unique.</p>
                        </div>
                      ) : (
                        results.plagiarizedSentences.map((item, idx) => (
                          <div
                            key={idx}
                            className="border border-red-200 rounded-lg p-4 bg-red-50"
                          >
                            <div className="flex items-start justify-between gap-4">
                              <div className="flex-1">
                                <p className="text-sm text-red-800 line-through mb-1">
                                  &ldquo;{item.originalText}&rdquo;
                                </p>
                                <p className="text-xs text-gray-600 mb-2">
                                  Matched: <span className="font-medium">{item.similarity}%</span> similarity
                                </p>
                                <div className="flex items-center gap-2 text-xs">
                                  <span className="text-gray-500">Source:</span>
                                  <a
                                    href={item.sourceUrl}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="text-indigo-600 hover:underline truncate max-w-xs"
                                  >
                                    {item.sourceUrl}
                                  </a>
                                </div>
                              </div>
                              <span className="text-xs font-medium bg-red-200 text-red-800 px-2 py-1 rounded">
                                {item.similarity}%
                              </span>
                            </div>
                            <div className="mt-3 border-t border-red-200 pt-3">
                              <p className="text-xs text-gray-500 mb-1">Suggested replacement:</p>
                              <p className="text-sm text-green-800 bg-green-50 border border-green-200 rounded p-2">
                                &ldquo;{item.replacement}&rdquo;
                              </p>
                            </div>
                          </div>
                        ))
                      )}
                    </div>
                  )}


                  {/* AI Detection Tab */}
                  {activeTab === "ai" && (
                    <div className="p-4 space-y-4">
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div className="border border-gray-200 rounded-lg p-4">
                          <div className="flex items-center gap-2 mb-3">
                            <Brain className="h-5 w-5 text-purple-600" />
                            <span className="font-medium text-sm">AI-Generated Probability</span>
                          </div>
                          <div className="relative h-4 bg-gray-200 rounded-full overflow-hidden">
                            <div
                              className="h-full bg-gradient-to-r from-purple-500 to-red-500 rounded-full transition-all"
                              style={{ width: `${results.aiDetection.aiProbability}%` }}
                            />
                          </div>
                          <div className="flex justify-between mt-2 text-xs text-gray-600">
                            <span>Human</span>
                            <span className="font-bold text-purple-700">
                              {results.aiDetection.aiProbability}% AI
                            </span>
                            <span>AI</span>
                          </div>
                        </div>
                        <div className="border border-gray-200 rounded-lg p-4">
                          <div className="flex items-center gap-2 mb-3">
                            <CheckCircle className="h-5 w-5 text-green-600" />
                            <span className="font-medium text-sm">Human Content</span>
                          </div>
                          <div className="relative h-4 bg-gray-200 rounded-full overflow-hidden">
                            <div
                              className="h-full bg-gradient-to-r from-green-400 to-green-600 rounded-full transition-all"
                              style={{ width: `${results.aiDetection.humanProbability}%` }}
                            />
                          </div>
                          <div className="flex justify-between mt-2 text-xs text-gray-600">
                            <span>0%</span>
                            <span className="font-bold text-green-700">
                              {results.aiDetection.humanProbability}% Human
                            </span>
                            <span>100%</span>
                          </div>
                        </div>
                      </div>


                      {/* Fingerprint */}
                      {showFingerprint && (
                        <div className="border border-gray-200 rounded-lg p-4">
                          <div className="flex items-center gap-2 mb-3">
                            <Fingerprint className="h-5 w-5 text-indigo-600" />
                            <span className="font-medium text-sm">Document Fingerprint</span>
                            <span className="text-xs bg-amber-100 text-amber-700 px-1.5 py-0.5 rounded">PRO</span>
                          </div>
                          <div className="bg-gray-900 text-green-400 rounded-lg p-3 font-mono text-xs overflow-x-auto">
                            {results.aiDetection.fingerprint}
                          </div>
                          <p className="text-xs text-gray-500 mt-2">
                            Unique document fingerprint for tracking and verification
                          </p>
                        </div>
                      )}

                      {/* AI Patterns Detected */}
                      <div className="border border-gray-200 rounded-lg p-4">
                        <h4 className="font-medium text-sm mb-3">AI Writing Patterns Detected</h4>
                        <div className="space-y-2">
                          {results.aiDetection.patterns.map((pattern, idx) => (
                            <div
                              key={idx}
                              className="flex items-center gap-2 text-sm text-gray-700"
                            >
                              <AlertTriangle className="h-3.5 w-3.5 text-amber-500 shrink-0" />
                              {pattern}
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>
                  )}


                  {/* Rewritten Text Tab */}
                  {activeTab === "replaced" && (
                    <div className="p-4">
                      <div className="flex items-center justify-between mb-3">
                        <p className="text-sm text-gray-600">
                          All plagiarized sentences have been replaced with unique alternatives:
                        </p>
                        <div className="flex items-center gap-2">
                          <button
                            onClick={() => copyToClipboard(replacedText)}
                            className="inline-flex items-center gap-1 text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1.5 rounded-lg transition"
                          >
                            <Copy className="h-3.5 w-3.5" /> Copy
                          </button>
                          <button
                            onClick={() => {
                              const blob = new Blob([replacedText], { type: "text/plain" });
                              const url = URL.createObjectURL(blob);
                              const a = document.createElement("a");
                              a.href = url;
                              a.download = "rewritten-content.txt";
                              a.click();
                            }}
                            className="inline-flex items-center gap-1 text-xs bg-indigo-100 hover:bg-indigo-200 text-indigo-700 px-3 py-1.5 rounded-lg transition"
                          >
                            <Download className="h-3.5 w-3.5" /> Download
                          </button>
                        </div>
                      </div>
                      <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                        <p className="text-sm text-gray-800 whitespace-pre-wrap leading-relaxed">
                          {replacedText}
                        </p>
                      </div>
                      <p className="text-xs text-gray-500 mt-3 flex items-center gap-1">
                        <CheckCircle className="h-3.5 w-3.5 text-green-500" />
                        This rewritten content is 100% plagiarism-free and retains the original meaning.
                      </p>
                    </div>
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
