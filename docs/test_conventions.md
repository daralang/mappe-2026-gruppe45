# Testing conventions for this project

## Purpose

These conventions exist so unit tests verify that code does what
it is *supposed* to do (per the assignment specification and
domain rules), not merely that it does what it currently does.
A test derived from reading the implementation is circular: it
will pass even when the implementation is wrong.

The grading rubric weights this explicitly. A-level requires:
descriptive test names, Arrange-Act-Assert, tests for the
business-critical classes, **strong negative tests for all tested
classes**, exception testing, lifecycle methods (@BeforeEach
etc.) where appropriate, and for file reading: **test parsing,
not file access**.

## What to test (and what not to)

**Test (business-critical):** the domain logic that, if wrong,
breaks the game's core promises. Calculators (purchase/sale
gross, commission, tax, total), Transaction commit rules
(insufficient funds rejected, double-commit rejected, sale of
unowned share rejected), Player money/portfolio/archive
behaviour, Exchange buy/sell/advance, statistics
(TransactionStatsService, net worth, status thresholds),
file *parsing* (CSV format rules), and the read services.

**Do not write dedicated tests for:** trivial getters/setters,
records' generated accessors, or pure delegation with no logic.
Getters are exercised *indirectly* by the behavioural tests that
assert on the state they expose - that indirect coverage is
sufficient. A test whose body is `assertEquals(x, obj.getX())`
right after `new Obj(x)` tests the language, not the code.

**Do not test the GUI/view layer with unit tests.** JavaFX view
behaviour is out of scope for unit testing here.

## Derive expected values from the SPEC, not the code

This is the most important rule. When writing an assertion, the
expected value must come from the assignment specification or
domain rule, computed independently - never by running the method
mentally and asserting it returns what it returns.

Example - SaleCalculator tax (spec: "30% skatt på gevinst",
gevinst = bruttoverdi - avgift - kjøpskostnader):

- WRONG (circular): read calculateTax(), see the formula, assert
  it returns that formula's output.
- RIGHT (spec-derived): purchase price 100, sale price 200,
  quantity 10. Gross = 2000. Commission (1%) = 20. Purchase cost
  = 1000. Gain = 2000 - 20 - 1000 = 980. Expected tax = 30% of
  980 = 294. Assert calculateTax() == 294. If the code computes
  gain differently, this test fails - which is the point.
  Hand-compute expected values in the test (or as a comment showing
  the derivation) so a reviewer can see the value came from the
  spec, not the implementation.

## Negative and edge-case tests are mandatory, not optional

For every tested class, include negative tests. The rubric
explicitly rewards "svært gode negative tester for alle klassene
som testes". For each behaviour ask:

- What inputs are invalid? (null, negative quantity, zero, blank
  symbol, empty list) - assert the documented exception is
  thrown, with assertThrows, and check the exception type
  specifically.
- What are the boundaries? (exactly enough money vs one øre too
  little; threshold met exactly vs just under; one price in
  history vs none; first/last week)
- What are the "this should be rejected" rules from the spec?
  (buy with insufficient funds, sell unowned share, commit a
  transaction twice) - each gets its own negative test.
  A class with only happy-path tests does not meet the A bar no
  matter how many it has.

## Structure and naming - the project's established convention

New test classes MUST match the convention already used across
the existing suite. Do not invent a different style; consistency
is itself graded ("gode beskrivende navn", uniform structure).
The convention, derived from the existing tests, is:

### Outer test class

- Named `<ClassUnderTest>Test`, package-private (`class`, no
  `public`), in the mirrored package under `src/test/java`.
- Class-level JavaDoc that opens with a `{@link ClassUnderTest}`
  reference, one or two sentences on what is covered, and the
  sentence "All tests follow the AAA pattern." (Short utility/
  record classes may use a one-line JavaDoc instead.)
- Shared fixture built in a `@BeforeEach void setUp()` in the
  outer class. Constants (currencies, valid sample values) as
  `private static final` fields. Reusable construction helpers
  as `private static` methods (e.g. `nokStock(...)`,
  `makeShare(...)`); a BigDecimal equality helper
  `assertBigDecimalEquals` where money/precision is compared.
### @Nested grouping

- Grouping is done with `@Nested`, always. One `@Nested` class
  per public method under test, named in PascalCase after the
  method (`CalculateGross`, `Buy`, `GetNetWorth`, `Constructor`),
  annotated with `@DisplayName("methodName()")` - the display
  name is the method signature with parentheses.
- Cross-cutting behaviour that isn't tied to a single method gets
  a concept-named `@Nested` instead (`GavConsolidation`,
  `FrozenValues`, `LiquidationMethods`, `Loans`, `Watchlist`).
  This applies to pure validation/record classes too: group their
  constructor checks and behaviour in `@Nested` classes the same
  way (as `LoanOfferTest` and `WatchlistEntryTest` do) - a record
  having no "real" methods is not a reason to fall back to flat
  tests.
- A `@Nested` group may declare its OWN `@BeforeEach` (with a
  distinct name like `setUpOffer()`, `setUpEntry()`) when that
  group needs extra setup the rest of the class doesn't. Never
  introduce state that makes tests order-dependent; every test
  must pass in isolation and in any order.
- Do NOT use `// ---- ... ----` comment banners to group flat
  `@Test` methods. The banner is just a manual stand-in for
  grouping; once `@Nested` is used the banner duplicates what the
  group already expresses and becomes noise. A few early classes
  (`LeaderboardEntryTest`, `JsonLeaderboardFileHandlerTest`) use
  the banner-and-flat-test form - this is a known pre-existing
  deviation, not a sanctioned alternative. Do not use it in new
  code; do not mechanically rewrite those existing classes (same
  reasoning as the naming deviation below - working tests are not
  rearranged near the deadline; this is a stated, deliberate
  choice in the report).
### Two-layer test naming

Every `@Test` carries BOTH:

- `@DisplayName` in natural language, starting "Should " and
  stating the expected outcome and condition
  (`"Should return 30% of profit when sale is profitable"`,
  `"Should throw exception when quantity is negative"`).
- A camelCase method name, no underscores, that compresses the
  same statement (`returnsThirtyPercentOfProfitWhenProfitable`,
  `throwsExceptionWhenQuantityIsNegative`).
  This is the single required form for all NEW test classes. It is
  the dominant style in the existing suite - the form used by every
  core domain test (calculators, Exchange, Player's main body,
  Portfolio, Stock, Share, Purchase, Sale, TransactionArchive,
  GameService) - and is the project standard.

The underscore form `method_scenario`
(`markAsRead_returnsNewInstanceWithReadFlag`) appears in a number
of mostly smaller or later-added classes (the notification and
toast services, `LanguageManagerTest`, `LoanTest`, the loan/save
exception tests) and - importantly - inside two `@Nested` groups
of `PlayerTest` itself (`Loans`, `NotificationStorage`), which
otherwise follows the standard. A few formatter classes use a
third, bare form with neither "Should" nor underscores
(`ChangeFormatterTest`, `MoneyFormatterTest`). None of these is a
sanctioned alternative - they are pre-existing deviations. Do NOT
use any of them in new code. Existing classes that are
internally consistent (all-standard or all-deviation) are left
as-is: rewriting working tests near the deadline is risk for no
functional gain, and this is a stated, deliberate choice in the
report. The one case worth fixing is *within-class* mixing:
`PlayerTest` breaks the "never mix" rule below by using both
forms in one file. Renaming its underscore-named methods onto the
standard form is a pure, low-risk change (no logic touched) and
is the recommended cleanup, because internal inconsistency in the
flagship model test is the most visible to a reviewer. Within any
single class, only ever one form may appear - never mix.

### AAA

- Explicit `// Arrange`, `// Act`, `// Assert` comments in every
  test. Collapse to `// Act & Assert` when the action is a
  one-liner inside `assertThrows` / a single assertion. Keep the
  phases visually separated.
- One logical behaviour per test. Multiple asserts are fine when
  they verify one behaviour (state after a buy: money decreased
  AND share added AND archived); do not test unrelated
  behaviours in one method.
### BigDecimal assertions

Never assert BigDecimal equality with raw `assertEquals(a, b)`
when scale may differ - use `assertEquals(0, a.compareTo(b))` or
the `assertBigDecimalEquals` helper. (Raw equals is only
acceptable where the exact scale is itself the contract being
tested, e.g. a scale-preservation test.)

## Lifecycle annotations - only where they earn their place

The rubric rewards "@BeforeAll, @BeforeEach osv - **der det er
hensiktsmessig**" (where appropriate). The emphasis is on
*appropriate*. Over-use is as wrong as under-use, and a reviewer
who sees a `@BeforeEach` that half the tests ignore, or shared
mutable state that makes tests order-dependent, reads it as a
misunderstanding of what these annotations are for. Use them as
follows:

- **@BeforeEach** - the default and by far the most common here.
  Use it to build the fresh fixture each test needs (a new
  `Player`, `Portfolio`, `Exchange`, converter). It must produce
  a clean, independent state every time. A `@Nested` group adds
  its own `@BeforeEach` only when that group genuinely needs
  extra setup the rest of the class doesn't (see `Loans`,
  `Sell`, `Watchlist`). Do NOT put setup in `@BeforeEach` that
  only some tests in scope use - construct that locally in the
  tests that need it (the existing tests do exactly this: shared
  state in setUp, scenario-specific objects built inside the
  test).
- **@BeforeAll** - only when there is genuinely expensive,
  immutable, shared setup that it is safe to create once for the
  whole class (e.g. a costly read-only resource). For this
  project's model tests there is almost none: objects are cheap
  and must be fresh per test, so @BeforeAll is usually the WRONG
  choice - using it to share a mutable `Player`/`Exchange` across
  tests would create order dependence and is explicitly not
  wanted. Reach for it only with a concrete, stated reason; its
  absence in a model test is correct, not a gap.
- **@AfterEach / @AfterAll** - only when a test acquires
  something that must be released (a stream, a temp resource).
  Pure in-memory unit tests need no teardown; adding empty or
  pointless teardown methods is noise. If a test needs
  @AfterEach to undo shared state, that usually signals the
  state should not have been shared in the first place - prefer
  fixing the fixture over adding teardown.
  The test to apply: would removing the annotation force visible
  duplication or break isolation? If yes, it is appropriate. If it
  changes nothing, or only exists "because the rubric lists it",
  leave it out - the rubric rewards judgement, not mechanical
  presence.

## File reading: parsing only, never file access

Per the rubric, file-reading tests must test the parsing logic,
not disk I/O. Test the parser by feeding it an in-memory
Reader/String/InputStream with:

- valid rows
- comment lines (#) and blank lines (must be skipped)
- malformed rows (wrong field count, blank symbol, blank name,
  non-numeric price, negative/zero price) - each its own negative
  test asserting the documented exception
- a source with zero valid rows (EmptyStockFileException)
  Do NOT write tests that create temp files, read from the
  filesystem, or depend on a file on disk. If the handler only
  exposes a Path-based API, that's a design note for the report -
  test whatever parse method takes a Reader/Stream; if none exists,
  flag it rather than testing disk access.

## Exceptions

- Use assertThrows and assert the specific exception type, not a
  broad superclass.
- Where the project has custom exceptions, assert the custom
  type is thrown (not a generic RuntimeException).
- Where it makes sense, assert the exception message or a field
  carries the expected information - but don't assert on exact
  message strings that are i18n/locale-dependent.
## What a good test class looks like here

For a business-critical class: a @BeforeEach building the common
fixture; happy-path tests with spec-derived expected values;
boundary tests; one negative test per documented rejection rule
using assertThrows with the specific exception; names matching
the project's existing convention; AAA visible. No getter tests.
No tests that just re-run the implementation and assert its own
output.