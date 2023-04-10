package Metrics.Model

enum class Metric(val shortName: String, val fullName: String, val description: String) {
    // Chidamber and Kemerer (C&K) Metrics
//    WMC("CK_WMC", "Weighted Methods per Class", ""),
//    DIT("CK_DIT", "Depth of Inheritance Tree", ""),
//    NOC("CK_NOC", "Number of Children", ""),
//    CBO("CK_CBO", "Coupling Between Objects", ""),
//    RFC("CK_RFC", "Response For Class", ""),
//    LCOM("CK_LCOM", "Lack of Cohesion of Methods v1", ""),
//    // Other OO Metrics
    CYCLO("CYCLO", "Cyclomatic Complexity", ""),
    LoC("OO_LoC", "Lines of Code", ""),

    LINES("LINES", "Lines of Code", ""),
    ARGUMENTS("ARGUMENTS", "Number of Arguments", ""),
    LOCAL_VARIABLES("LOCAL_VARIABLES", "Number of Local Variables", ""),
    FUNCTION_CALLS("FUNCTION_CALLS", "Number of Function Calls", ""),
    CONDITIONAL_STATEMENTS("CONDITIONAL_STATEMENTS", "Number of Conditional Statements", ""),
    ITERATION_STATEMENTS("ITERATION_STATEMENTS", "Number of Iteration Statements", ""),
    RETURN_STATEMENTS("RETURN_STATEMENTS", "Number of Return Statements", ""),
    INPUT_STATEMENTS("INPUT_STATEMENTS", "Number of Input Statements", ""),
    OUTPUT_STATEMENTS("OUTPUT_STATEMENTS", "Number of Output Statements", ""),
    FUNCTION_ASSIGNMENTS("FUNCTION_ASSIGNMENTS", "Number of Assignments Through Function Calls", ""),
    SELECTION_STATEMENTS("SELECTION_STATEMENTS", "Number of Selection Statements", ""),
    ASSIGNMENT_STATEMENTS("ASSIGNMENT_STATEMENTS", "Number of Assignments", "");

//    LCOM2("OO_LCOM2", "Lack of Cohesion of Methods v2", ""),
//    LCOM3("OO_LCOM3", "Lack of Cohesion of Methods v3", ""),
//    Ca("OO_Ca", "Fan in = Afferent Coupling", ""),
//    Ce("OO_Ce", "Fan out = Efferent Coupling", ""),
//    // Other Metrics
//    NODA("OT_NODA", "Number of Declared Attributes", ""),
//    NOPA("OT_NOPA", "Number of Public Attributes", ""),
//    NOPRA("OT_NOPRA", "Number of Private Attributes", ""),
//    NODM("OT_NODM", "Number of Declared Methods", ""),
//    NOPM("OT_NOPM", "Number of Public Methods", ""),
//    NOPRM("OT_NOPRM", "Number of Private Methods", ""),
//    DNIF("OT_DNIF", "Depth of Nested IF", ""),
//    DNFOR("OT_DNFOR", "Depth of Nested FOR", ""),
//    NOECB("OT_NOECB", "Number of Empty Catch Blocks", ""),
//    NOSE("OT_NOSE", "Number of Signaled Exceptions", ""),
//    NORE("OT_NORE", "Number of Raised Exceptions", "");
}