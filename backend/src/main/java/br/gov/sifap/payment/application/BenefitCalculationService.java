package br.gov.sifap.payment.application;

import br.gov.sifap.beneficiary.api.BeneficiaryQuery;
import br.gov.sifap.payment.api.BenefitCalculation;
import br.gov.sifap.payment.api.CalculationResult;
import br.gov.sifap.payment.domain.BenefitCalculator;
import br.gov.sifap.payment.domain.EligibilityPolicy;
import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Cpf;
import br.gov.sifap.sharedkernel.error.ResourceNotFoundException;
import br.gov.sifap.socialprogram.api.SocialProgramCatalog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class BenefitCalculationService implements BenefitCalculation {

    private final BeneficiaryQuery beneficiaryQuery;
    private final SocialProgramCatalog socialProgramCatalog;
    private final EligibilityPolicy eligibilityPolicy;
    private final BenefitCalculator benefitCalculator;

    BenefitCalculationService(
            BeneficiaryQuery beneficiaryQuery,
            SocialProgramCatalog socialProgramCatalog,
            EligibilityPolicy eligibilityPolicy,
            BenefitCalculator benefitCalculator) {
        this.beneficiaryQuery = beneficiaryQuery;
        this.socialProgramCatalog = socialProgramCatalog;
        this.eligibilityPolicy = eligibilityPolicy;
        this.benefitCalculator = benefitCalculator;
    }

    @Override
    @Transactional(readOnly = true)
    public CalculationResult calculate(Cpf cpf, Competence competence) {
        var beneficiary = beneficiaryQuery.findByCpf(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiario nao encontrado"));
        var program = socialProgramCatalog.findByCode(beneficiary.programCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Programa social nao encontrado: " + beneficiary.programCode()));

        // A elegibilidade roda antes do calculo, como BATCHPGT.NSP:L369 antes de L381.
        var decision = eligibilityPolicy.evaluate(beneficiary, program);
        if (!decision.eligible()) {
            return CalculationResult.rejected(decision.returnCode(), String.join("; ", decision.reasons()));
        }

        return benefitCalculator.calculate(beneficiary, program);
    }
}
