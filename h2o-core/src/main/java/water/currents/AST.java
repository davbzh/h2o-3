package water.currents;

import java.util.HashMap;

/**
 * Abstract Syntax Tree
 *
 * Subclasses define the program semantics
 */
abstract class AST {
  // Subclasses define their execution.  Constants like Numbers & Strings just
  // return a ValXXX.  Constant functions also just return a ValFun.

  // ASTExec is Function application, and evaluates the 1st arg and calls
  // 'apply' to evaluate the remaining arguments.  Usually 'apply' is just
  // "exec all args" then apply a primitive function op to the args, but for
  // logical AND/OR and IF statements, one or more arguments may never be
  // evaluated (short-circuit evaluation).
  abstract Val exec( Env env );

  // Default action after the initial execution of a function.  Typically the
  // action is "execute all arguments, then apply a primitive action to the
  // arguments", but short-circuit evaluation may not execute all args.
  Val apply( Env env, AST asts[] ) { throw water.H2O.fail(); }

  // Short name (there's lots of the simple math primtives, and we want them to
  // fit on one line)
  abstract String str();
  @Override public String toString() { return str(); }

  // Built-in primitives, done after other namespace lookups happen
  static final HashMap<String,AST> PRIMS = new HashMap<>();
  static void init(AST ast) { PRIMS.put(ast.str(),ast); }
  static {
    // Math ops
    init(new ASTAnd ());
    init(new ASTDiv ());
    init(new ASTMul ());
    init(new ASTOr  ());
    init(new ASTPlus());
    init(new ASTSub ());

    // Relational
    init(new ASTGE());
    init(new ASTGT());
    init(new ASTLE());
    init(new ASTLT());
    init(new ASTEQ());
    init(new ASTNE());

    // Logical - includes short-circuit evaluation
    init(new ASTLAnd());
    init(new ASTLOr());
  }
}

/** A number.  Execution is just to return the constant. */
class ASTNum extends AST {
  final ValNum _d;
  ASTNum( Exec e ) { _d = new ValNum(Double.valueOf(e.token())); }
  @Override public String str() { return _d.toString(); }
  @Override Val exec( Env env ) { return _d; }
}

/** A String.  Execution is just to return the constant. */
class ASTStr extends AST {
  final ValStr _str;
  ASTStr(Exec e, char c) { _str = new ValStr(e.match(c)); }
  @Override public String str() { return _str.toString(); }
  @Override Val exec(Env env) { return _str; }
}

/** An ID.  Execution does lookup in the current scope. */
class ASTId extends AST {
  final String _id;
  ASTId(Exec e) { _id = e.token(); }
  @Override public String str() { return _id; }
  @Override Val exec(Env env) { return env.lookup(_id); }
}

/** A primitive operation.  Execution just returns the function.  *Application*
 *  (not execution) applies the function to the arguments. */
abstract class ASTPrim extends AST {
  final ValFun _fun;
  ASTPrim( ) { _fun = new ValFun(this); }
  @Override Val exec( Env env ) { return _fun; }
}
