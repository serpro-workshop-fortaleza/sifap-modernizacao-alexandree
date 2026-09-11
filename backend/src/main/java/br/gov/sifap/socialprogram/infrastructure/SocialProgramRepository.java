package br.gov.sifap.socialprogram.infrastructure;

import br.gov.sifap.socialprogram.domain.SocialProgram;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialProgramRepository extends JpaRepository<SocialProgram, String> {

    List<SocialProgram> findAllByOrderByCodeAsc();
}
