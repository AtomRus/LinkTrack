package backend.academy.linktracker.ai.summarization;

public class StubSummarizer implements Summarizer {

    @Override
    public String summarize(String text, int threshold) {
        if (text.length() <= threshold) {
            return text;
        }
        return text.substring(0, threshold) + "...";
    }
}
