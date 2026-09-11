package br.gov.sifap.socialprogram.application;

import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.socialprogram.api.ProgramParameters;
import br.gov.sifap.socialprogram.api.SocialProgramCatalog;
import br.gov.sifap.socialprogram.domain.SocialProgram;
import br.gov.sifap.socialprogram.infrastructure.SocialProgramRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SocialProgramCatalogService implements SocialProgramCatalog {

    private final SocialProgramRepository socialProgramRepository;

    SocialProgramCatalogService(SocialProgramRepository socialProgramRepository) {
        this.socialProgramRepository = socialProgramRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProgramParameters> findActive(String programCode, Competence competence) {
        return socialProgramRepository.findById(programCode)
                .filter(program -> program.isActiveOn(competence))
                .map(program -> program.toParameters(true));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProgramParameters> findByCode(String programCode) {
        return socialProgramRepository.findById(programCode).map(SocialProgram::toParameters);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgramParameters> list() {
        return socialProgramRepository.findAllByOrderByCodeAsc().stream()
                .map(SocialProgram::toParameters)
                .toList();
    }
}
