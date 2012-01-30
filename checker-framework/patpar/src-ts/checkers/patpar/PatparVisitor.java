package checkers.patpar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import patpar.ParTask;
import patpar.ParClosure;
import checkers.basetype.BaseTypeVisitor;
import checkers.javari.quals.Assignable;
import checkers.source.Result;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypes;
import checkers.util.ElementUtils;
import checkers.util.TreeUtils;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

/**
 * A type-checking visitor for the Javari mutability annotations (
 * {@code @ReadOnly}, {@code @Mutable} and {@code @Assignable}) that extends
 * BaseTypeVisitor.
 * 
 * @see BaseTypeVisitor
 */
public class PatparVisitor extends BaseTypeVisitor<PatparChecker> {

	final private AnnotationMirror READONLY, MUTABLE, POLYREAD, QREADONLY;
	TypeElement parClosureElt;
	PatparAnnotatedTypeFactory atypeFactory;
	Stack<Boolean> classElts = new Stack<>();
	
	/**
	 * Creates a new visitor for type-checking the Javari mutability
	 * annotations.
	 * 
	 * @param checker
	 *            the {@link PatparChecker} to use
	 * @param root
	 *            the root of the input program's AST to check
	 */
	public PatparVisitor(PatparChecker checker, CompilationUnitTree root) {
		super(checker, root);
		READONLY = checker.READONLY;
		MUTABLE = checker.MUTABLE;
		POLYREAD = checker.POLYREAD;
		QREADONLY = checker.QREADONLY;
//		checkForAnnotatedJdk();
		
		parClosureElt = checker.getProcessingEnvironment().getElementUtils().getTypeElement(ParClosure.class.getName());
		atypeFactory = (PatparAnnotatedTypeFactory) super.atypeFactory;
	}

	/**
	 * Ensures the class type is not {@code @PolyRead} outside a
	 * {@code @PolyRead} context.
	 */
	@Override
	public Void visitClass(ClassTree node, Void p) {
		System.err.printf("Visiting class %s\n", node.getSimpleName());

		if (atypeFactory.fromClass(node).hasEffectiveAnnotation(POLYREAD)
				&& !atypeFactory.getSelfType(node).hasEffectiveAnnotation(
						POLYREAD))
			checker.report(Result.failure("polyread.type"), node);
		
		TypeElement elt = TreeUtils.elementFromDeclaration(node);
		classElts.push(isClosureTypeElt(elt));
		try {
			return super.visitClass(node, p);
		} finally {
			classElts.pop();
		}
	}

	@Override
	public Void visitVariable(VariableTree node, Void p) {
		if (node.getName().toString().equals("l")) {
			System.err.printf("foo\n");
		}
		System.err.printf("Visiting variable %s (%s)\n",
				node.getName(),
				atypeFactory.getAnnotatedType(node));
		
		Element elt = TreeUtils.elementFromDeclaration(node);
		switch (elt.getKind()) {
		case FIELD: 
			if (classElts.peek()) {
				checker.report(Result.failure("closure.with.fields"), node);
			}
		}
		return super.visitVariable(node, p);
	}

	private boolean isClosureTypeElt(TypeElement typeElt) {
		return atypeFactory.isSubtype(typeElt, parClosureElt);
	}

	@Override
	public Void visitMethod(MethodTree node, Void p) {
		System.err.printf("  Visiting method %s\n", node.getName());
		
		if (classElts.peek()) {
			ExecutableElement elt = TreeUtils.elementFromDeclaration(node);
			Element old = atypeFactory.pushClosureScope(elt);
			try {
				return super.visitMethod(node, p);
			} finally {
				atypeFactory.popClosureScope(elt, old);
			}
		} else {
			return super.visitMethod(node, p);
		}
	}

	/**
	 * Checks whether the variable represented by the given type and tree can be
	 * assigned, causing a checker error otherwise.
	 */
	@Override
	protected void checkAssignability(AnnotatedTypeMirror varType, Tree varTree) {
		if (!(varTree instanceof ExpressionTree))
			return;
		Element varElt = varType.getElement();
		if (varElt != null
				&& atypeFactory.getDeclAnnotation(varElt, Assignable.class) != null)
			return;

		ExpressionTree expTree = (ExpressionTree) varTree;
		
		boolean variableLocalField = TreeUtils.isSelfAccess(expTree)
				&& varElt != null
				&& varElt.getKind().isField();
		
		// visitState.getMethodTree() is null when in static initializer block
		boolean inConstructor = visitorState.getMethodTree() == null
				|| TreeUtils.isConstructor(visitorState.getMethodTree());
		
		System.err.printf("inConstructor=%s variableLocalField=%s",
				inConstructor, variableLocalField);

		if (variableLocalField && !inConstructor) {
			// POTENTIAL JAVARI BUG (Javari code always used getSelfType() here)
			AnnotatedTypeMirror rcvr = atypeFactory.getReceiver(expTree);
			System.err.printf("rcvr=%s", rcvr);
			if (!rcvr.hasEffectiveAnnotation(MUTABLE)) {
				checker.report(Result.failure("ro.field"), expTree);
			}
		}

		if (varTree.getKind() == Tree.Kind.MEMBER_SELECT
				&& !TreeUtils.isSelfAccess((ExpressionTree) varTree)) {
			AnnotatedTypeMirror receiver = atypeFactory
					.getReceiver((ExpressionTree) varTree);
			if (receiver != null && !receiver.hasEffectiveAnnotation(MUTABLE))
				checker.report(Result.failure("ro.field"), varTree);
		}

		if (varTree.getKind() == Tree.Kind.ARRAY_ACCESS) {
			AnnotatedTypeMirror receiver = atypeFactory
					.getReceiver((ExpressionTree) varTree);
			if (receiver != null && !receiver.hasEffectiveAnnotation(MUTABLE))
				checker.report(Result.failure("ro.element"), varTree);
		}
	}

	/**
	 * Tests whether the tree expressed by the passed type tree contains a
	 * qualified primitive type on its qualified type, and if so emits an error.
	 * 
	 * @param tree
	 *            the AST type supplied by the user
	 */
	@Override
	public void validateTypeOf(Tree tree) {
		AnnotatedTypeMirror type;
		// It's quite annoying that there is no TypeTree
		switch (tree.getKind()) {
		case PRIMITIVE_TYPE:
		case PARAMETERIZED_TYPE:
		case TYPE_PARAMETER:
		case ARRAY_TYPE:
		case UNBOUNDED_WILDCARD:
		case EXTENDS_WILDCARD:
		case SUPER_WILDCARD:
			type = atypeFactory.getAnnotatedTypeFromTypeTree(tree);
			break;
		default:
			type = atypeFactory.getAnnotatedType(tree);
		}

		// Here we simply test for primitive types
		// they can only occur at raw or array most inner component type
		type = AnnotatedTypes.innerMostType(type);
		if (type.getKind().isPrimitive()) {
			if (type.hasEffectiveAnnotation(QREADONLY)
					|| type.hasEffectiveAnnotation(READONLY)
					|| type.hasEffectiveAnnotation(POLYREAD)) {
				checker.report(Result.failure("primitive.ro"), tree);
			}
		}
		super.validateTypeOf(tree);
	}
}
