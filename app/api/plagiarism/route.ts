import { NextRequest, NextResponse } from "next/server";

interface PlagiarismRequest {
  text: string;
  scanMode: "normal" | "deep" | "academic";
  excludeQuotes: boolean;
  excludeBibliography: boolean;
  detectAI: boolean;
}

// Simulated web sources database for matching
const WEB_SOURCES = [
  {
    pattern: /artificial intelligence|machine learning|deep learning/gi,
    url: "https://en.wikipedia.org/wiki/Artificial_intelligence",
    domain: "Wikipedia",
  },
  {
    pattern: /climate change|global warming|greenhouse gas/gi,
    url: "https://www.nasa.gov/climate-change",
    domain: "NASA",
  },
  {
    pattern: /quantum computing|quantum mechanics|superposition/gi,
    url: "https://www.ibm.com/quantum",
    domain: "IBM Research",
  },
  {
    pattern: /blockchain|cryptocurrency|decentralized/gi,
    url: "https://ethereum.org/en/developers/docs/",
    domain: "Ethereum Docs",
  },
  {
    pattern: /neural network|backpropagation|gradient descent/gi,
    url: "https://arxiv.org/abs/1706.03762",
    domain: "arXiv",
  },
];


// Paraphrase a sentence to produce a unique replacement
function paraphraseSentence(sentence: string): string {
  const replacements: Record<string, string> = {
    "is a": "represents a",
    "are used": "find application",
    "can be": "has the potential to be",
    "has been": "has long been",
    "in order to": "to effectively",
    "due to": "as a result of",
    "such as": "including",
    "as well as": "along with",
    "in addition": "furthermore",
    "however": "nevertheless",
    "therefore": "consequently",
    "moreover": "additionally",
    "significant": "considerable",
    "important": "crucial",
    "various": "numerous",
    "provide": "offer",
    "require": "necessitate",
    "demonstrate": "illustrate",
    "indicate": "suggest",
    "utilize": "employ",
    "implement": "put into practice",
    "achieve": "accomplish",
    "develop": "create",
    "increase": "enhance",
    "reduce": "minimize",
    "the process of": "the method of",
    "it is": "this is",
    "there are": "one can find",
    "which is": "that serves as",
  };

  let result = sentence;
  for (const [original, replacement] of Object.entries(replacements)) {
    const regex = new RegExp(`\\b${original}\\b`, "gi");
    result = result.replace(regex, replacement);
  }

  // If no replacements were made, restructure the sentence
  if (result === sentence) {
    const words = sentence.split(" ");
    if (words.length > 6) {
      const mid = Math.floor(words.length / 2);
      const firstHalf = words.slice(0, mid).join(" ");
      const secondHalf = words.slice(mid).join(" ");
      result = `${secondHalf}, which relates to ${firstHalf.toLowerCase()}`;
    } else {
      result = `In essence, ${sentence.toLowerCase().replace(/\.$/, "")} in a broader context.`;
    }
  }

  return result;
}


// Detect AI-written patterns
function detectAIPatterns(text: string): string[] {
  const patterns: string[] = [];
  
  // Check for overly formal transition usage
  const transitions = (text.match(/\b(furthermore|moreover|additionally|consequently|nevertheless)\b/gi) || []).length;
  if (transitions > 2) {
    patterns.push("Excessive use of formal transitions (common in AI text)");
  }

  // Check for repetitive sentence structure
  const sentences = text.split(/[.!?]+/).filter(s => s.trim().length > 0);
  const startsWithThe = sentences.filter(s => s.trim().startsWith("The")).length;
  if (startsWithThe > sentences.length * 0.4) {
    patterns.push("Repetitive sentence openings detected");
  }

  // Check for hedging language
  const hedging = (text.match(/\b(it is worth noting|it should be noted|one might argue|it is important to)\b/gi) || []).length;
  if (hedging > 1) {
    patterns.push("Hedging language patterns typical of AI generation");
  }

  // Check for list-like structure
  const listPatterns = (text.match(/\b(firstly|secondly|thirdly|finally|in conclusion)\b/gi) || []).length;
  if (listPatterns > 2) {
    patterns.push("Structured enumeration pattern (AI-typical organization)");
  }

  // Perplexity analysis (simulated)
  if (text.length > 200) {
    patterns.push("Low perplexity score detected in multiple segments");
  }

  // Burstiness check
  const avgWordLength = text.split(/\s+/).reduce((acc, w) => acc + w.length, 0) / text.split(/\s+/).length;
  if (avgWordLength > 5.5) {
    patterns.push("Uniform vocabulary complexity (low burstiness)");
  }

  if (patterns.length === 0) {
    patterns.push("No significant AI patterns detected - content appears human-written");
  }

  return patterns;
}


// Generate a document fingerprint
function generateFingerprint(text: string): string {
  let hash = 0;
  for (let i = 0; i < text.length; i++) {
    const chr = text.charCodeAt(i);
    hash = ((hash << 5) - hash) + chr;
    hash |= 0;
  }
  const hex = Math.abs(hash).toString(16).padStart(8, "0");
  const ts = Date.now().toString(16);
  return `FP-${hex.slice(0, 4)}-${hex.slice(4, 8)}-${ts.slice(-4)}-${Math.random().toString(16).slice(2, 6)}`.toUpperCase();
}

export async function POST(request: NextRequest) {
  const body: PlagiarismRequest = await request.json();
  const { text, scanMode, excludeQuotes, excludeBibliography, detectAI } = body;

  // Simulate processing delay based on scan mode
  const delay = scanMode === "deep" ? 3000 : scanMode === "academic" ? 4000 : 2000;
  await new Promise((resolve) => setTimeout(resolve, delay));

  let processedText = text;

  // Exclude quoted text if option is enabled
  if (excludeQuotes) {
    processedText = processedText.replace(/"[^"]*"/g, "");
    processedText = processedText.replace(/'[^']*'/g, "");
  }

  // Exclude bibliography section if option is enabled
  if (excludeBibliography) {
    const bibIndex = processedText.toLowerCase().indexOf("references");
    const bibIndex2 = processedText.toLowerCase().indexOf("bibliography");
    const cutIndex = Math.min(
      bibIndex > -1 ? bibIndex : Infinity,
      bibIndex2 > -1 ? bibIndex2 : Infinity
    );
    if (cutIndex !== Infinity) {
      processedText = processedText.slice(0, cutIndex);
    }
  }


  // Split into sentences for analysis
  const sentences = processedText
    .split(/(?<=[.!?])\s+/)
    .filter((s) => s.trim().length > 15);

  // Simulate plagiarism detection
  const plagiarizedSentences: Array<{
    originalText: string;
    matchedSource: string;
    sourceUrl: string;
    similarity: number;
    replacement: string;
  }> = [];

  for (const sentence of sentences) {
    for (const source of WEB_SOURCES) {
      if (source.pattern.test(sentence)) {
        // Reset regex lastIndex
        source.pattern.lastIndex = 0;
        const similarity = Math.floor(60 + Math.random() * 35);
        
        // Only flag if similarity is high enough based on scan mode
        const threshold = scanMode === "deep" ? 55 : scanMode === "academic" ? 50 : 65;
        
        if (similarity >= threshold) {
          plagiarizedSentences.push({
            originalText: sentence.trim(),
            matchedSource: source.domain,
            sourceUrl: source.url,
            similarity,
            replacement: paraphraseSentence(sentence.trim()),
          });
        }
        break;
      }
    }
  }

  // If no pattern matches, simulate some results for demo
  if (plagiarizedSentences.length === 0 && sentences.length > 3) {
    const numMatches = scanMode === "deep" ? 3 : scanMode === "academic" ? 2 : 1;
    for (let i = 0; i < Math.min(numMatches, sentences.length); i++) {
      const idx = Math.floor(Math.random() * sentences.length);
      const sentence = sentences[idx];
      if (sentence && sentence.length > 20) {
        plagiarizedSentences.push({
          originalText: sentence.trim(),
          matchedSource: "Web Content",
          sourceUrl: `https://example.com/source/${Math.random().toString(36).slice(2, 8)}`,
          similarity: Math.floor(55 + Math.random() * 40),
          replacement: paraphraseSentence(sentence.trim()),
        });
      }
    }
  }


  // Calculate scores
  const totalSentences = sentences.length || 1;
  const plagiarizedCount = plagiarizedSentences.length;
  const overallScore = Math.round((plagiarizedCount / totalSentences) * 100);
  const uniqueContent = 100 - overallScore;

  // AI Detection
  const aiPatterns = detectAI ? detectAIPatterns(text) : [];
  const aiProbability = detectAI
    ? Math.min(85, Math.max(5, aiPatterns.length * 15 + Math.floor(Math.random() * 20)))
    : 0;

  const result = {
    overallScore,
    uniqueContent,
    plagiarizedSentences,
    aiDetection: {
      aiProbability,
      humanProbability: 100 - aiProbability,
      fingerprint: generateFingerprint(text),
      patterns: aiPatterns,
    },
    sourcesFound: plagiarizedSentences.length,
    wordsScanned: text.trim().split(/\s+/).length,
    scanTime: parseFloat((delay / 1000 + Math.random()).toFixed(1)),
  };

  return NextResponse.json(result);
}
