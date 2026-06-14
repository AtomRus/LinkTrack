package backend.academy.linktracker.ai.summarization;

public interface Summarizer {

    String summarize(String text, int threshold);
}
