/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.helidon.sitegen.asciidoctor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

/**
 *
 */
public class RewriteSourcePreprocessor extends Preprocessor {

    private static final Pattern INCLUDE_BRACKET_PATTERN = Pattern.compile("// _include-(start|end)::(.*)");
    private static final Pattern INCLUDE_NUMBERED_PATTERN = Pattern.compile("// _include::(\\d*)-(\\d*):(.*)");

    private static final String BLOCK_DELIMITER = "----";
    static final String INCLUDE_NUMBERED_TEMPLATE = "// _include::%d-%d:%s";
    static final String INCLUDE_BRACKET_TEMPLATE = "// _include-%s::%s";


    static boolean isIncludeStart(String line) {
        Matcher m = INCLUDE_BRACKET_PATTERN.matcher(line);
        return (m.matches() && m.group(1).equals("start"));
    }

    static boolean isSourceStart(String line) {
        return line.startsWith("[source");
    }

    @Override
    public void process(Document document, PreprocessorReader reader) {

//        String includesAttr = (String) document.getAttribute(IncludePreprocessor.INCLUDES_ATTR_NAME);
//
//        /*
//         * We want to overwrite the input file with our augmented
//         * content. We get the augmented content not from the raw
//         * input (i.e., not using reader.lines()) but from the
//         * processed input (i.e., readLines()). But once we read that
//         * it seems we have to restore it so the rest of the extensions
//         * and the main processing can see the content.
//         */
//        List<String> processedContent = reader.readLines();
//        List<String> updatedContent = new ArrayList<>();
//
//        boolean isInSource = false;
//
//        int lineNumber = 0;
//        while (lineNumber < processedContent.size()) {
//            String line = processedContent.get(lineNumber);
//            if (line.startsWith("[source")) {
//                lineNumber = processSource(processedContent, updatedContent, lineNumber);
//            } else if (line.startsWith("include::")) {
//                lineNumber = processInclude(processedContent, updatedContent, lineNumber);
//            } else {
//                updatedContent.add(line);
//            }
//            lineNumber++;
//        }
//        String processedContentString = processedContent.stream()
//                .collect(Collectors.joining(System.lineSeparator()));
//        reader.restoreLines(processedContent);
////        System.err.println(String.format("In rewrite preproc: processed output is ----\n%s\n----",
////                processedContent));
    }

//    static int processSource(List<String> processedContent, List<String> updatedContent, int lineNumber) {
//
//        SourceBlockAnalyzer sba = new SourceBlockAnalyzer();
//
//        lineNumber = sba.run(processedContent, lineNumber);
//        updatedContent.addAll(sba.updatedSourceBlock());
//
//        return lineNumber;
//    }

    static int processInclude(List<String> processedContent, List<String> updatedContent, int lineNumber) {
        return lineNumber;
    }

    private static boolean isBlock(String line) {
        return line.equals(BLOCK_DELIMITER);
    }

    /**
     * Describes included content by starting and ending line number within the
     * content block of interest, the include target (path and other information
     * from the original include::), and the included text itself.
     *
     * The starting and ending line numbers exclude any bracketing comment lines
     * which might mark the included text.
     *
     * The "content block of interest" is either the body of a [source] block
     * (the region between the ---- delimiters) if the include appears within
     * one, or is a fictitious (synthetic) block that starts immediately after
     * the _include-start marker.
     */
    static class IncludeAnalyzer {

        private final int startWithinBlock;
        private final int endWithinBlock;
        private final List<String> body;
        private final String includeTarget;

        IncludeAnalyzer(List<String> content, int startOfBlock,
                int startWithinBlock, int endWithinBlock, List<String> body, String includeTarget) {
            this.startWithinBlock = startWithinBlock;
            this.endWithinBlock = endWithinBlock;
            this.includeTarget = includeTarget;
            this.body = new ArrayList<>(body);
        }

        IncludeAnalyzer(List<String> content, int startOfBlock,
                int startWithinBlock, int endWithinBlock, String includeTarget) {

            this(content, startOfBlock, startWithinBlock, endWithinBlock,
                    buildBody(content, startOfBlock, startWithinBlock, endWithinBlock),
                    includeTarget);
        }

        private static List<String> buildBody(List<String> content, int startOfBlock,
                int startWithinBlock, int endWithinBlock) {
            List<String> b = new ArrayList<>();
            for (int i = startWithinBlock; i <= endWithinBlock; i++) {
                b.add(content.get(startOfBlock + i));
            }
            return b;
        }

        /**
         * Creates a new IncludeAnalyzer from the specified content, the
         * starting point of the block within that content, and the numbered
         * include descriptor. The block (as indicated by the starting point)
         * does not include any include descriptor comment.
         *
         * @param content text containing the numbered include block
         * @param startOfBlock start of the block containing included content
         * @param numberedInclude descriptor for the include (start, end, target)
         */
        static IncludeAnalyzer fromNumberedInclude(
                List<String> content,
                int startOfBlock,
                String numberedInclude) {
            Matcher m = INCLUDE_NUMBERED_PATTERN.matcher(numberedInclude);
            if ( ! m.matches()) {
                throw new IllegalStateException("Expected numbered include but did not match expected pattern - " + numberedInclude);
            }
            int startWithinBlock = Integer.parseInt(m.group(1));
            int endWithinBlock = Integer.parseInt(m.group(2));
            IncludeAnalyzer result = new IncludeAnalyzer(
                content,
                startOfBlock,
                startWithinBlock,
                endWithinBlock,
                m.group(3));

            return result;
        }

        /**
         * Parse a bracketed include block, starting at the specified line
         * within the content.
         *
         * @param content lines containing the bracketed include to be parsed
         * @oaran startOfBlock where the relevant block begins in the content
         * @param lineNumber starting point of the bracketed include
         * @return updated line number within the content, after the bracketed include
         */
        static IncludeAnalyzer consumeBracketedInclude(List<String> content, int startOfBlock, AtomicInteger lineNumber) {
            String line = content.get(lineNumber.get());
            Matcher m = INCLUDE_BRACKET_PATTERN.matcher(line);
            if ( ! (m.matches() && m.group(1).equals("start"))) {
                return null;
            }
            int startWithinBlock = lineNumber.getAndIncrement() - startOfBlock;
            String includeTarget = m.group(2);
            boolean endFound;
            List<String> body = new ArrayList<>();
            do {
                line = content.get(lineNumber.getAndIncrement());
                m.reset(line);
                if ( ! (endFound = (m.matches() && m.group(1).equals("end")))) {
                    body.add(line);
                }
            } while ( ! endFound);
            // Subtract 2 next because we are already past the // _include-end and
            // we don't even want to include that line in the calculation of the
            // ending point within the block of the included content.
            int endWithinBlock = startWithinBlock + body.size() - 1;
            return new IncludeAnalyzer(
                    content,
                    startOfBlock,
                    startWithinBlock,
                    endWithinBlock,
                    body,
                    includeTarget);
        }

        /**
         * Format the included content as bracketed included lines.
         *
         * @return lines with the included content bracketed with start and end comments
         */
        List<String> asBracketed() {
            List<String> result = new ArrayList<>();
            result.add(String.format(INCLUDE_BRACKET_TEMPLATE, "start", includeTarget));
            for (String bodyLine : body) {
                result.add(bodyLine);
            }
            result.add(String.format(INCLUDE_BRACKET_TEMPLATE, "end", includeTarget));
            return result;
        }

        /**
         * Format the numbered descriptor for the included content.
         *
         * @return AsciiDoc comment describing the include using line numbers
         */
        String numberedDescriptor() {
            return String.format(INCLUDE_NUMBERED_TEMPLATE, startWithinBlock, endWithinBlock, includeTarget);
        }

        /**
         * Returns the text associated with this include.
         * @return the included content
         */
        List<String> body() {
            return body;
        }

        int startWithinBlock() {
            return startWithinBlock;
        }

        int endWithinBlock() {
            return endWithinBlock;
        }

        String includeTarget() {
            return includeTarget;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + this.startWithinBlock;
            hash = 37 * hash + this.endWithinBlock;
            hash = 37 * hash + Objects.hashCode(this.body);
            hash = 37 * hash + Objects.hashCode(this.includeTarget);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IncludeAnalyzer other = (IncludeAnalyzer) obj;
            if (this.startWithinBlock != other.startWithinBlock) {
                return false;
            }
            if (this.endWithinBlock != other.endWithinBlock) {
                return false;
            }
            if (!Objects.equals(this.includeTarget, other.includeTarget)) {
                return false;
            }
            if (!Objects.equals(this.body, other.body)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "IncludeAnalyzer{" + "startWithinBlock=" + startWithinBlock + ", endWithinBlock=" + endWithinBlock + ", body=" + body + ", includeTarget=" + includeTarget + '}';
        }


    }

    /**
     * Consumes and analyzes a portion of content that is a [source] block that
     * might contain bracketed includes.
     */
    static class SourceBlockAnalyzer {

        private String sourceBlockDecl = null;
        private final List<IncludeAnalyzer> includes = new ArrayList<>();
        private final List<String> body = new ArrayList<>();

        private List<String> preamble = new ArrayList<>();

        static SourceBlockAnalyzer consumeSourceBlock(List<String> content, AtomicInteger lineNumber) {

            SourceBlockAnalyzer sba = new SourceBlockAnalyzer();

            sba.run(content, lineNumber);

            return sba;
        }

        private void run(List<String> content, AtomicInteger aLineNumber) {
            sourceBlockDecl = content.get(aLineNumber.getAndIncrement());

            preamble = collectPreamble(content, aLineNumber);
            int blockStartLineNumber = aLineNumber.get();

            String line;

            while ( ! isBlock(line = content.get(aLineNumber.getAndIncrement()))) {
                Matcher m = INCLUDE_BRACKET_PATTERN.matcher(line);
                if (m.matches() && m.groupCount() > 0 && m.group(1).equals("start")) {
                    aLineNumber.decrementAndGet();
                    IncludeAnalyzer ia = IncludeAnalyzer.consumeBracketedInclude(content, blockStartLineNumber, aLineNumber);
                    includes.add(ia);
                    body.addAll(ia.body());
                } else {
                    body.add(line);
                }
            }
        }

        List<String> updatedSourceBlock() {
            List<String> result = new ArrayList<>();
            result.add(sourceBlockDecl);
            result.addAll(preamble);
            for (IncludeAnalyzer ia : includes) {
                result.add(ia.numberedDescriptor());
            }
            result.add(BLOCK_DELIMITER);
            result.addAll(body);
            result.add(BLOCK_DELIMITER);
            return result;
        }

        List<IncludeAnalyzer> sourceIncludes() {
            return includes;
        }

        private List<String> collectPreamble(List<String> content, AtomicInteger lineNumber) {
            final List<String> result = new ArrayList<>();
            String line;
            while ( ! isBlock(line = content.get(lineNumber.getAndIncrement()))) {
                result.add(line);
            }
            return result;
        }
    }

}
