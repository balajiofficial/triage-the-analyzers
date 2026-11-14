package team.projectpulse.section;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SectionSpecsTest {

    private Locale originalDefault;

    @BeforeEach
    void saveDefaultLocale() {
        this.originalDefault = Locale.getDefault();
    }

    @AfterEach
    void restoreDefaultLocale() {
        Locale.setDefault(this.originalDefault);
    }

    /**
     * The search lowercases the column in SQL (criteriaBuilder.lower) and the search text in Java, and the two
     * have to agree. In a Turkish locale Java lowercases "I" to a dotless "ı", while MySQL's lower() keeps "i";
     * under utf8mb4_0900_ai_ci the pattern "%ıntro%" then matches no section named "Intro ...".
     */
    @Test
    @SuppressWarnings("unchecked")
    void containsSectionNameLowercasesTheSearchTextTheSameWayInEveryLocale() {
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));

        Root<Section> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Path<String> sectionName = mock(Path.class);
        Expression<String> loweredSectionName = mock(Expression.class);
        given(root.<String>get("sectionName")).willReturn(sectionName);
        given(criteriaBuilder.lower(sectionName)).willReturn(loweredSectionName);

        SectionSpecs.containsSectionName("INTRO").toPredicate(root, query, criteriaBuilder);

        ArgumentCaptor<String> pattern = ArgumentCaptor.forClass(String.class);
        verify(criteriaBuilder).like(eq(loweredSectionName), pattern.capture());
        assertThat(pattern.getValue()).isEqualTo("%intro%");
    }

}
