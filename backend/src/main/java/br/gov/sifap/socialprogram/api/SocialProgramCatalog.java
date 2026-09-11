package br.gov.sifap.socialprogram.api;

import br.gov.sifap.sharedkernel.domain.Competence;
import java.util.List;
import java.util.Optional;

/**
 * Unica porta de entrada de outros modulos no Catalogo de Programas Sociais.
 * Somente leitura nesta feature.
 */
public interface SocialProgramCatalog {

    /** Devolve os parametros apenas quando o programa esta ativo e vigente na competencia. */
    Optional<ProgramParameters> findActive(String programCode, Competence competence);

    Optional<ProgramParameters> findByCode(String programCode);

    List<ProgramParameters> list();
}
