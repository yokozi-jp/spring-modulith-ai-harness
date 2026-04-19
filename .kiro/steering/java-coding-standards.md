# Java コーディング規約 — PMD 7 準拠

本プロジェクトでは PMD 7 の全カテゴリを除外なしで有効化している。
すべてのルールが適用されるため、以下の規約に従い違反を防ぐこと。

参考: https://docs.pmd-code.org/pmd-doc-7.1.0/pmd_rules_java.html

## プロジェクト構成

- Java 25, Spring Boot 4.0.5, Spring Modulith（モジュラーモノリス）
- Google Java Format（Spotless 経由）— フォーマットを手動調整しない
- JSpecify + NullAway 有効（`com.example.demo` パッケージ、JSpecifyMode=true）
- テストコードでは NullAway 無効
- Lombok 使用可（compileOnly + annotationProcessor）
- jOOQ によるデータアクセス（JPA/Hibernate は不使用）
- PMD ルールセット: `backend/config/pmd/ruleset.xml`

## 本プロジェクトで使用済みの @SuppressWarnings

以下の抑制は特定のコンテキストで使用済みかつ許容されている:

- `PMD.UseUtilityClass` — `@SpringBootApplication` クラスおよびテストランチャークラス
- `PMD.MethodArgumentCouldBeFinal` — main メソッドクラス（Google Java Format はパラメータに `final` を付与しない）
- `PMD.AtLeastOneConstructor` — テストクラスおよび `@TestConfiguration` クラス（コンストラクタが不要な場合）
- `PMD.UnitTestShouldIncludeAssert` — コンテキストロードのスモークテスト
- `PMD.TestClassWithoutTestCases` — テストサポートクラス（例: `TestcontainersConfiguration`）

`@SuppressWarnings` を追加する際は、必ず `"PMD.RuleName"` 形式を使用し、必要なルールのみを指定すること。

---

## ベストプラクティス (`bestpractices.xml`)

適用ルール（59 件、deprecated 除外）:

- **AbstractClassWithoutAbstractMethod** — abstract クラスに abstract メソッドがない。
- **AccessorClassGeneration** — 外部からの private コンストラクタ呼び出しでアクセサが生成される。
- **AccessorMethodGeneration** — 他クラスから private フィールド/メソッドへのアクセスでアクセサメソッドが生成される。
- **ArrayIsStoredDirectly** — 配列を受け取るコンストラクタ/メソッドはコピーを保存すべき。
- **AvoidMessageDigestField** — MessageDigest をフィールドに持つとスレッド安全性の問題が生じる。
- **AvoidPrintStackTrace** — `printStackTrace()` ではなくロガーを使用する。
- **AvoidReassigningCatchVariables** — catch 変数の再代入を避ける。
- **AvoidReassigningLoopVariables** — ループ変数の再代入はバグの原因になる。
- **AvoidReassigningParameters** — メソッド引数への再代入は可読性を下げる。
- **AvoidStringBufferField** — StringBuffer/StringBuilder をフィールドに持つとメモリリークの原因になる。
- **AvoidUsingHardCodedIP** — ハードコードされた IP アドレスを避ける。
- **CheckResultSet** — ResultSet のナビゲーションメソッドの戻り値を必ず確認する。
- **ConstantsInInterface** — インターフェースに定数を定義しない。
- **DefaultLabelNotLastInSwitch** — default ラベルは switch の最後に置く。
- **DoubleBraceInitialization** — 二重ブレース初期化は匿名クラスを生成するため避ける。
- **EnumComparison** — enum の比較には `==` を使い `equals()` を避ける。
- **ExhaustiveSwitchHasDefault** — 網羅的 switch に default は不要。
- **ForLoopCanBeForeach** — foreach に置き換え可能な for ループを検出。
- **ForLoopVariableCount** — for ループの制御変数が多すぎる。
- **GuardLogStatement** — ログレベルが有効か事前にチェックする。
- **ImplicitFunctionalInterface** — `@FunctionalInterface` が明示されていない関数型インターフェースを検出。
- **JUnit4SuitesShouldUseSuiteAnnotation** — JUnit 4 では `@RunWith(Suite.class)` を使用する。
- **JUnit5TestShouldBePackagePrivate** — JUnit 5 テストはパッケージプライベートにすべき。
- **JUnitUseExpected** — JUnit 4 では `@Test(expected)` を使用する。
- **LabeledStatement** — ラベル付き文の使用を検出。
- **LiteralsFirstInComparisons** — 文字列比較ではリテラルを左辺に置く。
- **LooseCoupling** — 実装型ではなくインターフェース型で宣言する。
- **MethodReturnsInternalArray** — 内部配列を直接返さずコピーを返す。
- **MissingOverride** — オーバーライドメソッドに `@Override` を付ける。
- **NonExhaustiveSwitch** — switch 文は網羅的にする。
- **OneDeclarationPerLine** — 1 行に 1 宣言。
- **PreserveStackTrace** — catch ブロック内で例外を投げる際は元の例外を参照する。
- **PrimitiveWrapperInstantiation** — プリミティブラッパーのコンストラクタは非推奨、`valueOf` を使う。
- **RelianceOnDefaultCharset** — デフォルト文字セットに依存せず明示的に指定する。
- **ReplaceEnumerationWithIterator** — Enumeration を Iterator に置き換える。
- **ReplaceHashtableWithMap** — Hashtable を Map に置き換える。
- **ReplaceVectorWithList** — Vector を ArrayList に置き換える。
- **SimplifiableTestAssertion** — より具体的なアサーションメソッドで簡略化できるテストアサーションを検出。
- **SystemPrintln** — `System.out/err.print` ではなくロガーを使用する。
- **UnitTestAssertionsShouldIncludeMessage** — アサーションにメッセージを含める。
- **UnitTestContainsTooManyAsserts** — テストのアサーション数が多すぎる。
- **UnitTestShouldIncludeAssert** — テストに少なくとも 1 つのアサーションを含める。
- **UnitTestShouldUseAfterAnnotation** — `tearDown()` メソッドに適切なアノテーションを付ける。
- **UnitTestShouldUseBeforeAnnotation** — `setUp()` メソッドに適切なアノテーションを付ける。
- **UnitTestShouldUseTestAnnotation** — テストメソッドに `@Test` アノテーションを付ける。
- **UnnecessaryVarargsArrayCreation** — 可変長引数に明示的な配列生成は不要。
- **UnnecessaryWarningSuppression** — 不要な `@SuppressWarnings` を検出。
- **UnusedAssignment** — 使用されない代入を検出。
- **UnusedFormalParameter** — 使用されないメソッド引数を検出。
- **UnusedLabel** — 使用されないラベルを検出。
- **UnusedLocalVariable** — 使用されないローカル変数を検出。
- **UnusedPrivateField** — 使用されない private フィールドを検出。
- **UnusedPrivateMethod** — 使用されない private メソッドを検出。
- **UseCollectionIsEmpty** — `size() == 0` ではなく `isEmpty()` を使用する。
- **UseEnumCollections** — enum キーには `EnumSet`/`EnumMap` を使用する。
- **UseStandardCharsets** — `StandardCharsets` 定数を使用する。
- **UseTryWithResources** — try-with-resources を使用する。
- **UseVarargs** — 可変長引数を使用する。
- **WhileLoopWithLiteralBoolean** — `do {} while (true)` より `while (true) {}` を使う。

## コードスタイル (`codestyle.xml`)

適用ルール（59 件、deprecated 除外）:

- **AtLeastOneConstructor** — 非静的クラスには少なくとも 1 つのコンストラクタを宣言する。
- **AvoidDollarSigns** — 変数/メソッド/クラス/インターフェース名にドル記号を使わない。
- **AvoidProtectedFieldInFinalClass** — final クラスで protected フィールドを使わない。
- **AvoidProtectedMethodInFinalClassNotExtending** — final クラスで protected メソッドを使わない。
- **AvoidUsingNativeCode** — JNI の使用はポータビリティを下げ保守負担を増やす。
- **BooleanGetMethodName** — boolean を返すメソッドは `is/has/can/will` で始める。
- **CallSuperInConstructor** — コンストラクタで `super()` を呼ぶ。
- **ClassNamingConventions** — 型宣言の命名規則を設定可能。
- **CommentDefaultAccessModifier** — デフォルトアクセス修飾子にはコメントを付ける。
- **ConfusingTernary** — else 句がある if 式で否定条件を避ける。
- **ControlStatementBraces** — 制御文にブレースを強制する。
- **EmptyControlStatement** — 空の制御文を検出。
- **EmptyMethodInAbstractClassShouldBeAbstract** — abstract クラスの空メソッドは abstract にすべき。
- **ExtendsObject** — 明示的な `extends Object` は不要。
- **FieldDeclarationsShouldBeAtStartOfClass** — フィールド宣言はクラスの先頭に置く。
- **FieldNamingConventions** — フィールド宣言の命名規則を設定可能。
- **FinalParameterInAbstractMethod** — インターフェースメソッドの引数に final は無意味。
- **ForLoopShouldBeWhileLoop** — while ループに簡略化できる for ループを検出。
- **FormalParameterNamingConventions** — メソッド/ラムダの仮引数の命名規則を設定可能。
- **IdenticalCatchBranches** — 同一の catch ブランチはマルチキャッチにまとめる。
- **LambdaCanBeMethodReference** — メソッド参照に置き換え可能なラムダ式を検出。
- **LinguisticNaming** — 言語的命名アンチパターンを検出。
- **LocalHomeNamingConvention** — Session EJB の Local Home インターフェースは `LocalHome` サフィックスを付ける。
- **LocalInterfaceSessionNamingConvention** — Session EJB の Local インターフェースは `Local` サフィックスを付ける。
- **LocalVariableCouldBeFinal** — 一度だけ代入されるローカル変数は final にできる。
- **LocalVariableNamingConventions** — ローカル変数の命名規則を設定可能。
- **LongVariable** — 長すぎる変数名は可読性を下げる。
- **MDBAndSessionBeanNamingConvention** — MessageDrivenBean/SessionBean は `Bean` サフィックスを付ける。
- **MethodArgumentCouldBeFinal** — 再代入されないメソッド引数は final にできる。
- **MethodNamingConventions** — メソッド宣言の命名規則を設定可能。
- **ModifierOrder** — JLS 推奨の修飾子順序を強制する。
- **NoPackage** — パッケージ定義がないクラスを検出。
- **OnlyOneReturn** — メソッドの出口は 1 つにする。
- **PackageCase** — パッケージ名に大文字を含めない。
- **PrematureDeclaration** — 使用前に早すぎる変数宣言を検出。
- **RemoteInterfaceNamingConvention** — Session EJB の Remote インターフェースにサフィックスを付けない。
- **RemoteSessionInterfaceNamingConvention** — Session EJB の Remote Home インターフェースは `Home` サフィックスを付ける。
- **ShortClassName** — 短すぎるクラス名を検出。
- **ShortMethodName** — 短すぎるメソッド名を検出。
- **ShortVariable** — 短すぎる変数名を検出。
- **TooManyStaticImports** — static import の過剰使用を検出。
- **TypeParameterNamingConventions** — 型パラメータの命名規則を設定可能。
- **UnnecessaryAnnotationValueElement** — アノテーションの唯一の要素が value の場合、明示不要。
- **UnnecessaryBoxing** — 不要なボクシング/アンボクシングを検出。
- **UnnecessaryCast** — 不要なキャストを検出。
- **UnnecessaryConstructor** — デフォルトコンストラクタと同一の不要なコンストラクタを検出。
- **UnnecessaryFullyQualifiedName** — import 済みの完全修飾名は不要。
- **UnnecessaryImport** — 不要な import 文を検出。
- **UnnecessaryInterfaceDeclaration** — 親で既に実装済みのインターフェース宣言は不要。
- **UnnecessaryModifier** — インターフェース/アノテーションの暗黙的修飾子は不要。
- **UnnecessaryReturn** — 不要な return 文を検出。
- **UnnecessarySemicolon** — 不要なセミコロンを検出。
- **UseDiamondOperator** — ダイヤモンド演算子 `<>` を使用する。
- **UseExplicitTypes** — `var` の使用に関するルール。
- **UseShortArrayInitializer** — 配列の短縮初期化構文を使用する。
- **UseUnderscoresInNumericLiterals** — 数値リテラルにアンダースコアを使用する。
- **UselessParentheses** — 不要な括弧を検出。
- **UselessQualifiedThis** — 同一クラス内の不要な修飾 this を検出。
- **VariableCanBeInlined** — 宣言直後に return/throw されるローカル変数はインライン化できる。

## 設計 (`design.xml`)

適用ルール（41 件、deprecated 除外）:

- **AbstractClassWithoutAnyMethod** — メソッドを持たない abstract クラスはデータコンテナの可能性がある。
- **AvoidDeeplyNestedIfStmts** — 深くネストした if 文は可読性と保守性を下げる。
- **AvoidRethrowingException** — catch ブロックで同じ例外を再スローするだけのコードは冗長。
- **AvoidThrowingNewInstanceOfSameException** — catch した例外を同じ型で再ラップして投げるのは冗長。
- **AvoidThrowingNullPointerException** — NullPointerException を手動でスローしない。
- **AvoidThrowingRawExceptionTypes** — RuntimeException/Exception/Throwable/Error を直接スローせずサブクラスを使う。
- **AvoidUncheckedExceptionsInSignatures** — throws 句に非チェック例外を宣言しない。
- **ClassWithOnlyPrivateConstructorsShouldBeFinal** — private コンストラクタのみのクラスは final にすべき。
- **CognitiveComplexity** — メソッドの認知的複雑度が高すぎる。
- **CollapsibleIfStatements** — ネストした if 文を `&&` で結合できる。
- **CouplingBetweenObjects** — オブジェクト間の結合度が高すぎる。
- **CyclomaticComplexity** — 循環的複雑度が高すぎる。
- **DataClass** — データ保持のみで振る舞いがないクラスを検出。
- **DoNotExtendJavaLangError** — Error を継承しない。
- **ExceptionAsFlowControl** — 例外をフロー制御に使わない。
- **ExcessiveImports** — import 数が多すぎると結合度が高い。
- **ExcessiveParameterList** — メソッド引数が多すぎる。
- **ExcessivePublicCount** — public メンバーが多すぎるクラスを検出。
- **FinalFieldCouldBeStatic** — コンパイル時定数の final フィールドは static にできる。
- **GodClass** — 責務が多すぎる巨大クラスを検出。
- **ImmutableField** — 初期化後に変更されないフィールドは final にできる。
- **InvalidJavaBean** — JavaBeans 仕様に準拠していない Bean を検出。
- **LawOfDemeter** — デメテルの法則違反を検出。
- **LogicInversion** — 論理否定の代わりに逆の演算子を使う。
- **LoosePackageCoupling** — パッケージ階層外からの不正なクラス使用を検出。
- **MutableStaticState** — 非 private な static フィールドは final にすべき。
- **NcssCount** — NCSS（非コメントソース行数）が閾値を超えている。
- **NPathComplexity** — NPath 複雑度が高すぎる。
- **PublicMemberInNonPublicType** — 非 public 型で public メンバーを宣言しても実質的に公開されない。
- **SignatureDeclareThrowsException** — 汎用の `java.lang.Exception` を throws 宣言しない。
- **SimplifiedTernary** — `condition ? true : expr` 等の三項演算子を簡略化できる。
- **SimplifyBooleanExpressions** — 不要な boolean 比較を検出。
- **SimplifyBooleanReturns** — boolean を返す不要な if-else を検出。
- **SimplifyConditional** — instanceof の前の null チェックは不要。
- **SingularField** — ローカル変数に変換可能なフィールドを検出。
- **SwitchDensity** — switch 文のラベルあたりの文数が多すぎる。
- **TooManyFields** — フィールド数が多すぎるクラスを検出。
- **TooManyMethods** — メソッド数が多すぎるクラスを検出。
- **UselessOverridingMethod** — 親メソッドを呼ぶだけの無意味なオーバーライドを検出。
- **UseObjectForClearerAPI** — 引数が多い public メソッドはオブジェクトでまとめる。
- **UseUtilityClass** — static メソッドのみのクラスはユーティリティクラスにする。

## ドキュメント (`documentation.xml`)

適用ルール（6 件、deprecated 除外）:

- **CommentContent** — コメント内容の不適切な表現を検出。
- **CommentRequired** — 特定の言語要素に Javadoc コメントが必要かどうかを設定可能。
- **CommentSize** — コメントのサイズが制限を超えていないか検出。
- **DanglingJavadoc** — クラス/メソッド/フィールドに属さない Javadoc コメントを検出。
- **UncommentedEmptyConstructor** — 空のコンストラクタにコメントがない。
- **UncommentedEmptyMethodBody** — 空のメソッドボディにコメントがない。

## エラー防止 (`errorprone.xml`)

適用ルール（89 件、deprecated 除外）:

- **AssignmentInOperand** — オペランド内での代入はコードを複雑にする。
- **AssignmentToNonFinalStatic** — static フィールドへの安全でない代入を検出。
- **AvoidAccessibilityAlteration** — リフレクションによるアクセス制御の変更を避ける。
- **AvoidAssertAsIdentifier** — `assert` は予約語のため識別子として使わない。
- **AvoidBranchingStatementAsLastInLoop** — ループ末尾の分岐文はバグの可能性がある。
- **AvoidCallingFinalize** — `finalize()` をアプリケーションコードから呼ばない。
- **AvoidCatchingGenericException** — 汎用例外（Exception/RuntimeException/NullPointerException）を catch しない。
- **AvoidDecimalLiteralsInBigDecimalConstructor** — `new BigDecimal(0.1)` は正確に 0.1 にならない。
- **AvoidDuplicateLiterals** — 重複する文字列リテラルは定数に抽出する。
- **AvoidEnumAsIdentifier** — `enum` は予約語のため識別子として使わない。
- **AvoidFieldNameMatchingMethodName** — フィールド名とメソッド名の一致は紛らわしい。
- **AvoidFieldNameMatchingTypeName** — フィールド名と型名の一致は紛らわしい。
- **AvoidInstanceofChecksInCatchClause** — catch 内の instanceof チェックは個別の catch 句に分ける。
- **AvoidLiteralsInIfCondition** — 条件文にハードコードされたリテラルを避ける。
- **AvoidMultipleUnaryOperators** — 複数の単項演算子の使用は紛らわしい。
- **AvoidUsingOctalValues** — 先頭ゼロの整数リテラルは 8 進数として解釈される。
- **BrokenNullCheck** — null チェックのロジックが壊れている。
- **CallSuperFirst** — メソッドの先頭で super を呼ぶべき。
- **CallSuperLast** — メソッドの末尾で super を呼ぶべき。
- **CheckSkipResult** — `skip()` の戻り値を確認する。
- **ClassCastExceptionWithToArray** — `toArray()` に正しい型の配列を渡す。
- **CloneMethodMustBePublic** — `clone()` メソッドは public にすべき。
- **CloneMethodMustImplementCloneable** — `clone()` は Cloneable を実装したクラスでのみ定義する。
- **CloneMethodReturnTypeMustMatchClassName** — `clone()` の戻り値型はクラス名と一致させる。
- **CloseResource** — リソース（AutoCloseable）は必ずクローズする。
- **CollectionTypeMismatch** — コレクション操作で型の不一致を検出。
- **CompareObjectsWithEquals** — オブジェクト参照の比較には `equals()` を使う。
- **ComparisonWithNaN** — NaN との比較は常に false になる。
- **ConfusingArgumentToVarargsMethod** — 可変長引数メソッドへの紛らわしい引数を検出。
- **ConstructorCallsOverridableMethod** — コンストラクタからオーバーライド可能なメソッドを呼ばない。
- **DetachedTestCase** — テストアノテーションが付いていないテストメソッドを検出。
- **DoNotCallGarbageCollectionExplicitly** — `System.gc()` を明示的に呼ばない。
- **DoNotExtendJavaLangThrowable** — Throwable ではなく Exception/RuntimeException を継承する。
- **DoNotHardCodeSDCard** — SD カードパスをハードコードしない。
- **DoNotTerminateVM** — Web アプリケーションで `System.exit()` を呼ばない。
- **DoNotThrowExceptionInFinally** — finally ブロック内で例外をスローしない。
- **DontUseFloatTypeForLoopIndices** — ループインデックスに浮動小数点型を使わない。
- **EmptyCatchBlock** — 空の catch ブロックは例外を握りつぶす。
- **EmptyFinalizer** — 空の finalize メソッドは不要。
- **EqualsNull** — null チェックに `equals()` を使わず `==` を使う。
- **FinalizeDoesNotCallSuperFinalize** — `finalize()` の最後に `super.finalize()` を呼ぶ。
- **FinalizeOnlyCallsSuperFinalize** — `super.finalize()` を呼ぶだけの finalize は不要。
- **FinalizeOverloaded** — `finalize()` に引数を持たせない。
- **FinalizeShouldBeProtected** — `finalize()` は protected にすべき。
- **IdempotentOperations** — 冪等な操作（効果のない操作）を検出。
- **IdenticalConditionalBranches** — 条件分岐の true/false が同一の処理。
- **ImplicitSwitchFallThrough** — switch の暗黙的フォールスルーを検出。
- **InstantiationToGetClass** — `getClass()` のためだけにインスタンス化しない。
- **InvalidLogMessageFormat** — ログメッセージのプレースホルダーと引数の数が不一致。
- **JUnitSpelling** — `setUp`/`tearDown` のスペルミスを検出。
- **JUnitStaticSuite** — `suite()` メソッドは public static にする。
- **JumbledIncrementer** — ループのインクリメンタが混乱している。
- **MethodWithSameNameAsEnclosingClass** — メソッド名がクラス名と同じ。
- **MisplacedNullCheck** — null チェックの位置が不適切。
- **MissingSerialVersionUID** — Serializable クラスに serialVersionUID がない。
- **MissingStaticMethodInNonInstantiatableClass** — private コンストラクタのみで static メソッドがないクラスは使用不可。
- **MoreThanOneLogger** — クラスに複数のロガーを定義しない。
- **NonCaseLabelInSwitch** — switch 内の非 case ラベルは紛らわしい。
- **NonSerializableClass** — Serializable クラスの全フィールドはシリアライズ可能にする。
- **NonStaticInitializer** — 非 static 初期化ブロックは紛らわしい。
- **NullAssignment** — 変数への null 代入は避ける。
- **OverrideBothEqualsAndHashcode** — `equals()` と `hashCode()` は両方オーバーライドする。
- **OverrideBothEqualsAndHashCodeOnComparable** — Comparable 実装クラスは `equals()` と `hashCode()` を両方オーバーライドする。
- **ProperCloneImplementation** — `clone()` は `super.clone()` で実装する。
- **ProperLogger** — ロガーは private static final で正しいクラスに関連付ける。
- **ReplaceJavaUtilCalendar** — `java.util.Calendar` の代わりに `java.time` API を使う。
- **ReplaceJavaUtilDate** — `java.util.Date` の代わりに `java.time` API を使う。
- **ReturnEmptyCollectionRatherThanNull** — null ではなく空のコレクションを返す。
- **ReturnFromFinallyBlock** — finally ブロックから return しない。
- **SimpleDateFormatNeedsLocale** — SimpleDateFormat にはロケールを指定する。
- **SingleMethodSingleton** — シングルトンの `getInstance` をオーバーロードしない。
- **SingletonClassReturningNewInstance** — シングルトンが毎回新しいインスタンスを返している。
- **StaticEJBFieldShouldBeFinal** — EJB の static フィールドは final にすべき。
- **StringBufferInstantiationWithChar** — StringBuffer/StringBuilder のコンストラクタに char を渡すと int に変換される。
- **SuspiciousEqualsMethodName** — `equals` に似たメソッド名だが正しくオーバーライドしていない。
- **SuspiciousHashcodeMethodName** — `hashCode` に似たメソッド名だが正しくオーバーライドしていない。
- **SuspiciousOctalEscape** — 文字列リテラル内の疑わしい 8 進エスケープを検出。
- **TestClassWithoutTestCases** — テストクラス名だがテストメソッドがない。
- **UnconditionalIfStatement** — 常に true/false の条件を持つ if 文を検出。
- **UnnecessaryBooleanAssertion** — boolean リテラルを使ったアサーションは不要。
- **UnnecessaryCaseChange** — `equalsIgnoreCase()` を使う方が効率的。
- **UnnecessaryConversionTemporary** — プリミティブから String への変換に一時オブジェクトを使わない。
- **UnsupportedJdkApiUsage** — `sun.*` や `jdk.internal.*` パッケージの API を使わない。
- **UnusedNullCheckInEquals** — null チェック後は自身の `equals()` を呼ぶ。
- **UseCorrectExceptionLogging** — 例外ログには String と Throwable の 2 引数版を使う。
- **UseEqualsToCompareStrings** — 文字列比較に `==` ではなく `equals()` を使う。
- **UselessPureMethodCall** — 副作用のないメソッドの戻り値を無視している。
- **UseLocaleWithCaseConversions** — `toLowerCase()`/`toUpperCase()` にはロケールを明示する。
- **UseProperClassLoader** — J2EE では `Thread.currentThread().getContextClassLoader()` を使う。

## マルチスレッド (`multithreading.xml`)

適用ルール（11 件、deprecated 除外）:

- **AvoidSynchronizedAtMethodLevel** — メソッドレベルの synchronized は仮想スレッドをピン留めしパフォーマンス問題を起こす。
- **AvoidSynchronizedStatement** — synchronized 文は仮想スレッドをピン留めしパフォーマンス問題を起こす。
- **AvoidThreadGroup** — ThreadGroup はスレッド安全でないメソッドを含むため避ける。
- **AvoidUsingVolatile** — `volatile` の使用は Java メモリモデルの深い理解を要する。
- **DoNotUseThreads** — J2EE ではスレッドの直接使用は禁止されている。
- **DontCallThreadRun** — `Thread.run()` ではなく `Thread.start()` を呼ぶ。
- **DoubleCheckedLocking** — ダブルチェックロッキングは部分的に構築されたオブジェクトを返す可能性がある。
- **NonThreadSafeSingleton** — スレッド安全でないシングルトンは不正な状態変更を引き起こす。
- **UnsynchronizedStaticFormatter** — `java.text.Format` のインスタンスは一般にスレッド安全でない。
- **UseConcurrentHashMap** — マルチスレッドアクセスには `ConcurrentHashMap` を使用する。
- **UseNotifyAllInsteadOfNotify** — `notify()` ではなく `notifyAll()` を使用する。

## パフォーマンス (`performance.xml`)

適用ルール（24 件、deprecated 除外）:

- **AddEmptyString** — 空文字列との結合による型変換は非効率。`String.valueOf()` を使う。
- **AppendCharacterWithChar** — StringBuffer/StringBuilder の append で文字列ではなく char を使う。
- **AvoidArrayLoops** — 配列コピーには `Arrays.copyOf` や `System.arraycopy` を使う。
- **AvoidCalendarDateCreation** — `Calendar` は重量級オブジェクト。`java.time` API を使う。
- **AvoidFileStream** — FileInputStream/FileOutputStream は finalizer を持つため GC に影響する。
- **AvoidInstantiatingObjectsInLoops** — ループ内でのオブジェクト生成をループ外に移動できないか確認する。
- **BigIntegerInstantiation** — `BigInteger.ZERO`/`ONE`/`TEN` や `BigDecimal` の定数を使う。
- **ConsecutiveAppendsShouldReuse** — 連続する append 呼び出しはチェーンする。
- **ConsecutiveLiteralAppends** — 連続するリテラルの append は 1 つの文字列にまとめる。
- **InefficientEmptyStringCheck** — `trim().length()` ではなく `isBlank()`（Java 11+）を使う。
- **InefficientStringBuffering** — StringBuffer/StringBuilder の append で非リテラルの結合を避ける。
- **InsufficientStringBufferDeclaration** — StringBuffer/StringBuilder の初期容量を適切に設定する。
- **OptimizableToArrayCall** — `toArray(new T[0])` でゼロサイズ配列を渡す。
- **RedundantFieldInitializer** — Java のデフォルト値と同じ初期化は冗長。
- **StringInstantiation** — `new String()` は不要。文字列リテラルを直接使う。
- **StringToString** — String に対する `toString()` 呼び出しは不要。
- **TooFewBranchesForSwitch** — 分岐が少なすぎる switch は if-else の方が適切。
- **UseArrayListInsteadOfVector** — スレッド安全が不要なら Vector より ArrayList を使う。
- **UseArraysAsList** — 配列から List を作るには `Arrays.asList()` を使う。
- **UseIOStreamsWithApacheCommonsFileItem** — Apache Commons FileItem では `get()`/`getString()` よりストリームを使う。
- **UseIndexOfChar** — 単一文字の検索には `indexOf(char)` を使う。
- **UselessStringValueOf** — 文字列結合時の `String.valueOf()` は不要。
- **UseStringBufferForStringAppends** — `+=` による文字列結合は StringBuilder を使う。
- **UseStringBufferLength** — StringBuffer の長さ確認には `length()` を使う。

## セキュリティ (`security.xml`)

適用ルール（2 件、deprecated 除外）:

- **HardCodedCryptoKey** — 暗号鍵をハードコードしない。ソースコード外に保管する。
- **InsecureCryptoIv** — 暗号の初期化ベクトルをハードコードしない。ランダム生成する。

---

## Spring Modulith 規約

- 各モジュールは `com.example.demo` の直下サブパッケージとする（例: `com.example.demo.order`）。
- モジュールはルートパッケージのクラスで API を公開する。内部クラスはサブパッケージに配置する。
- 他モジュールの内部（サブパッケージ）クラスに直接アクセスしない。
- モジュール間通信には Spring Modulith イベント（`ApplicationEventPublisher`）を使用する。

## JSpecify / NullAway 規約

- NullAway は `AnnotatedPackages=com.example.demo`、`JSpecifyMode=true` で設定済み。
- `com.example.demo` 内のすべてのパラメータ、戻り値、フィールドはデフォルトで非 null。
- 値が `null` になり得る場合は `org.jspecify.annotations.@Nullable` でアノテーションを付ける。
- テストコードでは NullAway 無効 — `@Nullable` アノテーションは任意。
- 新しいモジュールパッケージを作成する際は `package-info.java` に `@NullMarked` を付与する:

  ```java
  @NullMarked
  package com.example.demo.modulename;

  import org.jspecify.annotations.NullMarked;
  ```

## Lombok 規約

- DI（コンストラクタインジェクション）には `@RequiredArgsConstructor` を使用する。
- ロガーフィールドを手動宣言する代わりに `@Slf4j` を使用する。
- `@Getter`、`@Builder`、`@Value`（不変）を適宜使用する。
- Lombok 生成コンストラクタは PMD の `AtLeastOneConstructor` ルールを満たす。
- 本番コードの可変クラスに `@Data` を使わない — 不変には `@Value`、それ以外は明示的な getter/setter を優先する。
- `@Setter` を使わない — 可変性を最小限に保つため、フィールドへの書き込みが必要な場合は明示的な setter メソッドか、ビルダーパターン（`@Builder`）を使用する。

## jOOQ 規約

- すべてのデータベースクエリに jOOQ DSL を使用する — 生の SQL 文字列を書かない。
- jOOQ 生成コードはビルドディレクトリにあり、PMD 解析から除外されている。
- jOOQ の `Record` 型をドメインオブジェクトに明示的にマッピングする — jOOQ 型を API レイヤーに漏洩させない。
