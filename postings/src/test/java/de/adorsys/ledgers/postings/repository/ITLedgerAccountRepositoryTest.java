package de.adorsys.ledgers.postings.repository;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.utils.Ids;
import de.adorsys.ledgers.tests.PostingsApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("ITLedgerAccountRepositoryTest-db-entries.xml")
@DatabaseTearDown(value={"ITLedgerAccountRepositoryTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ITLedgerAccountRepositoryTest {
	
	@Autowired
	private LedgerAccountRepository ledgerAccountRepository;
	
	@Autowired
	private LedgerRepository ledgerRepository;

	@Autowired
	private LedgerAccountTypeRepository ledgerAccountTypeRepository;
	
	@Test
	public void test_create_ledger_account_ok() {
		Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
		Assume.assumeNotNull(ledger);
		// Type of Balance sheet account
		LedgerAccountType ledgerAccountType = ledgerAccountTypeRepository.findById("805UO1hITPHxQq16OuGvw_BS").orElse(null);
		Assume.assumeNotNull(ledgerAccountType);
		
		LedgerAccount parentAccount = ledgerAccountRepository.findOptionalByLedgerAndName(ledger, "3.0.0").orElse(null);
		Assume.assumeNotNull(parentAccount);
		
		LedgerAccount ledgerAccount = LedgerAccount.builder()
				.id(Ids.id())
				.created(LocalDateTime.now())
				.user("Sample User")
				.shortDesc("Long lasting liability")
				.longDesc("Long lasting liability (from 1 year to 3 years)")
				.name("Long lasting liability")
				.ledger(ledger)
				.accountType(ledgerAccountType)
				.parent(parentAccount)
				.build();
		ledgerAccountRepository.save(ledgerAccount);
	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_account_no_ledger() {
		LedgerAccountType ledgerAccountType = ledgerAccountTypeRepository.findById("805UO1hITPHxQq16OuGvw_BS").orElse(null);
		Assume.assumeNotNull(ledgerAccountType);
		LedgerAccount ledgerAccount = LedgerAccount.builder().id(Ids.id())
				.name("Sample Ledger Account")
				.user("Sample User")
				.accountType(ledgerAccountType)
				.build();
		ledgerAccountRepository.save(ledgerAccount);
	}

	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_account_no_type() {
		Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
		Assume.assumeNotNull(ledger);
		// Type of Balance sheet account
		LedgerAccountType ledgerAccountType = ledgerAccountTypeRepository.findById("805UO1hITPHxQq16OuGvw_BS").orElse(null);
		Assume.assumeNotNull(ledgerAccountType);
		LedgerAccount parentAccount = LedgerAccount.builder().ledger(ledger).name("3.0.0").build();

		LedgerAccount ledgerAccount = LedgerAccount.builder().id(Ids.id())
				.name("Sample Ledger Account")
				.user("Sample User")
				.parent(parentAccount)
				.ledger(ledger)
				.build();
		ledgerAccountRepository.save(ledgerAccount);
	}
	
	@Test(expected=DataIntegrityViolationException.class)
	public void test_create_ledger_account_unique_constrain_violation_ledger_name_validFrom() {
		LedgerAccount ledgerAccount = ledgerAccountRepository.findById("xVgaTPMcRty9ik3BTQDh1Q_BS").orElse(null);
		Assume.assumeNotNull(ledgerAccount);
		LedgerAccountType ledgerAccountType = ledgerAccountTypeRepository.findById("805UO1hITPHxQq16OuGvw_BS").orElse(null);
		Assume.assumeNotNull(ledgerAccountType);
		LedgerAccount ledgerAccount2 = LedgerAccount.builder().id(Ids.id())
				.name(ledgerAccount.getName())
				.user("Sample User")
				.parent(ledgerAccount.getParent())
				.accountType(ledgerAccountType)
				.ledger(ledgerAccount.getLedger()).build();
		ledgerAccountRepository.save(ledgerAccount2);
	}

	@Test
	public void test_find_by_ledger_and_name_ok() {
		Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
		Assume.assumeNotNull(ledger);
		Optional<LedgerAccount> found = ledgerAccountRepository.findOptionalByLedgerAndName(ledger, "BS");
		Assert.assertTrue(found.isPresent());
	}

	@Test
	public void test_find_by_ledger_and_level_and_account_type_and_valid_from_before_and_valid_to_after(){
		Ledger ledger = ledgerRepository.findById("Zd0ND5YwSzGwIfZilhumPg").orElse(null);
		Assume.assumeNotNull(ledger);
		LedgerAccountType ledgerAccountType = ledgerAccountTypeRepository.findById("805UO1hITPHxQq16OuGvw_BS").orElse(null);
		Assume.assumeNotNull(ledgerAccountType);
		List<LedgerAccount> found = ledgerAccountRepository
				.findByLedgerAndLevelAndAccountType(ledger, 0, ledgerAccountType);
		assertEquals(1, found.size());

	}
}
