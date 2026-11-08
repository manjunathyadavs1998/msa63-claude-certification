package course.ai;

import static org.assertj.core.api.Assertions.assertThat;

import course.ai.model.ModelPort;
import course.ai.model.ScriptedModel;
import course.ai.rag.InMemorySearch;
import course.ai.rag.RagService;
import course.ai.rag.RagService.Chunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests the service's decisions. The search is a stand-in, so quality isn't measured
 * here. */
class RagServiceTest {

    private InMemorySearch search;
    private ScriptedModel model;
    private RagService rag;

    @BeforeEach
    void setUp() {
        search = new InMemorySearch();
        search.add(new Chunk("hr-1", "leave.md", "2026-03-01",
                "Parental leave is 26 weeks for employees."), "all-staff");
        search.add(new Chunk("fin-1", "close.md", "2026-05-01",
                "Quarter close runs for 5 working days."), "finance");
        search.add(new Chunk("pay-1", "refunds.md", "2026-02-01",
                "A refund takes 5 to 10 days."), "all-staff");
        search.person("asha", "finance", "all-staff");
        search.person("ravi", "all-staff");
        model = new ScriptedModel(request -> ScriptedModel.text("Answer from documents."));
        rag = new RagService(model, search, search);
    }

    private void report(String label, RagService.Answer answer) {
        System.out.printf("LAB|%-34s %-12s sources=%s modelCalls=%d%n", label,
                answer.outcome(),
                answer.sources().stream().map(Chunk::id).toList(), model.seen.size());
    }

    @Test
    void permissionsDecideWhatEvenGetsRetrieved() {
        RagService.Answer allowed = rag.ask("asha", "How long is quarter close?");
        report("asha asks about quarter close", allowed);
        assertThat(allowed.sources()).extracting(Chunk::id).contains("fin-1");

        int before = model.seen.size();
        RagService.Answer denied = rag.ask("ravi", "How long is quarter close?");
        report("ravi asks about quarter close", denied);
        assertThat(denied.outcome()).isEqualTo("NOT_FOUND");
        assertThat(model.seen).hasSize(before);
    }

    @Test
    void directoryFailureFailsClosed() {
        RagService.Answer answer = rag.ask("unknown-person", "How long is quarter close?");
        report("directory can't answer", answer);
        assertThat(answer.outcome()).isEqualTo("UNAVAILABLE");
        assertThat(model.seen).isEmpty();
    }

    @Test
    void promptPutsDocumentsFirstAndKeepsSourcesAndDates() {
        rag.ask("ravi", "How do I get my money back on a refund?");
        ModelPort.UserText sent =
                (ModelPort.UserText) model.seen.getFirst().history().getFirst();
        System.out.println("LAB|prompt: " + sent.text().replace("\n", " "));
        assertThat(sent.text()).startsWith("<documents>");
        assertThat(sent.text()).contains("<published>2026-02-01</published>");
        assertThat(sent.text())
                .endsWith("Question: How do I get my money back on a refund?");
    }
}
