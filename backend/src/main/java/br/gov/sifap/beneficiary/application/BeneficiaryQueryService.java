package br.gov.sifap.beneficiary.application;

import br.gov.sifap.beneficiary.api.BeneficiaryQuery;
import br.gov.sifap.beneficiary.api.BeneficiarySnapshot;
import br.gov.sifap.beneficiary.api.BeneficiarySummary;
import br.gov.sifap.beneficiary.domain.Beneficiary;
import br.gov.sifap.beneficiary.infrastructure.BeneficiaryRepository;
import br.gov.sifap.sharedkernel.domain.Cpf;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class BeneficiaryQueryService implements BeneficiaryQuery {

    private final BeneficiaryRepository beneficiaryRepository;

    BeneficiaryQueryService(BeneficiaryRepository beneficiaryRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BeneficiarySnapshot> findByCpf(Cpf cpf) {
        return beneficiaryRepository.findByCpf(cpf.value()).map(Beneficiary::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiarySnapshot> findAllOrderedByCpf() {
        return beneficiaryRepository.findAllByOrderByCpfAsc().stream()
                .map(Beneficiary::toSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiarySummary> list() {
        return beneficiaryRepository.findAllByOrderByCpfAsc().stream()
                .map(Beneficiary::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiarySummary> findSummariesByCpf(Collection<Cpf> cpfs) {
        if (cpfs.isEmpty()) {
            return List.of();
        }
        var values = cpfs.stream().map(Cpf::value).toList();
        return beneficiaryRepository.findByCpfIn(values).stream()
                .map(Beneficiary::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByProgramCode(String programCode) {
        return beneficiaryRepository.countByProgramCode(programCode);
    }
}
