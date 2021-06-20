/*
 * Copyright (c) 2009-2021 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.snakefangox.worldshell.math;

import java.util.logging.Logger;

/**
 * <code>Matrix3d</code> defines a 3x3 matrix. Matrix data is maintained
 * internally and is accessible via the get and set methods. Convenience methods
 * are used for matrix operations as well as generating a matrix from a given
 * set of values.
 *
 * @author Mark Powell
 * @author Joshua Slack
 */
public final class Matrix3d implements Cloneable, java.io.Serializable {

	static final long serialVersionUID = 1;

	private static final Logger logger = Logger.getLogger(Matrix3d.class.getName());
	/**
	 * the element in row 0, column 0
	 */
	protected double m00;
	/**
	 * the element in row 0, column 1
	 */
	protected double m01;
	/**
	 * the element in row 0, column 2
	 */
	protected double m02;
	/**
	 * the element in row 1, column 0
	 */
	protected double m10;
	/**
	 * the element in row 1, column 1
	 */
	protected double m11;
	/**
	 * the element in row 1, column 2
	 */
	protected double m12;
	/**
	 * the element in row 2, column 0
	 */
	protected double m20;
	/**
	 * the element in row 2, column 1
	 */
	protected double m21;
	/**
	 * the element in row 2, column 2
	 */
	protected double m22;
	/**
	 * an instance of the zero matrix (all elements = 0)
	 */
	public static final Matrix3d ZERO = new Matrix3d(0, 0, 0, 0, 0, 0, 0, 0, 0);
	/**
	 * an instance of the identity matrix (diagonals = 1, other elements = 0)
	 */
	public static final Matrix3d IDENTITY = new Matrix3d();

	/**
	 * Constructor instantiates a new <code>Matrix3d</code> object. The
	 * initial values for the matrix is that of the identity matrix.
	 */
	public Matrix3d() {
		loadIdentity();
	}

	/**
	 * constructs a matrix with the given values.
	 *
	 * @param m00 0x0 in the matrix.
	 * @param m01 0x1 in the matrix.
	 * @param m02 0x2 in the matrix.
	 * @param m10 1x0 in the matrix.
	 * @param m11 1x1 in the matrix.
	 * @param m12 1x2 in the matrix.
	 * @param m20 2x0 in the matrix.
	 * @param m21 2x1 in the matrix.
	 * @param m22 2x2 in the matrix.
	 */
	public Matrix3d(double m00, double m01, double m02, double m10, double m11,
					double m12, double m20, double m21, double m22) {
		this.m00 = m00;
		this.m01 = m01;
		this.m02 = m02;
		this.m10 = m10;
		this.m11 = m11;
		this.m12 = m12;
		this.m20 = m20;
		this.m21 = m21;
		this.m22 = m22;
	}

	/**
	 * <code>copy</code> transfers the contents of a given matrix to this
	 * matrix. If a null matrix is supplied, this matrix is set to the identity
	 * matrix.
	 *
	 * @param matrix
	 *            the matrix to copy.
	 * @return this
	 */
	public Matrix3d set(Matrix3d matrix) {
		if (null == matrix) {
			loadIdentity();
		} else {
			m00 = matrix.m00;
			m01 = matrix.m01;
			m02 = matrix.m02;
			m10 = matrix.m10;
			m11 = matrix.m11;
			m12 = matrix.m12;
			m20 = matrix.m20;
			m21 = matrix.m21;
			m22 = matrix.m22;
		}
		return this;
	}

	/**
	 * <code>get</code> retrieves a value from the matrix at the given position.
	 *
	 * @param i   the row index.
	 * @param j   the column index.
	 * @return the value at (i, j).
	 * @throws IllegalArgumentException if either index is invalid
	 */
	@SuppressWarnings("fallthrough")
	public double get(int i, int j) {
		switch (i) {
			case 0:
				switch (j) {
					case 0:
						return m00;
					case 1:
						return m01;
					case 2:
						return m02;
				}
			case 1:
				switch (j) {
					case 0:
						return m10;
					case 1:
						return m11;
					case 2:
						return m12;
				}
			case 2:
				switch (j) {
					case 0:
						return m20;
					case 1:
						return m21;
					case 2:
						return m22;
				}
		}

		logger.warning("Invalid matrix index.");
		throw new IllegalArgumentException("Invalid indices into matrix.");
	}

	/**
	 * <code>set</code> places a given value into the matrix at the given
	 * position.
	 *
	 * @param i   the row index.
	 * @param j   the column index.
	 * @param value
	 *            the value for (i, j).
	 * @return this
	 * @throws IllegalArgumentException if either index is invalid
	 */
	@SuppressWarnings("fallthrough")
	public Matrix3d set(int i, int j, double value) {
		switch (i) {
			case 0:
				switch (j) {
					case 0:
						m00 = value;
						return this;
					case 1:
						m01 = value;
						return this;
					case 2:
						m02 = value;
						return this;
				}
			case 1:
				switch (j) {
					case 0:
						m10 = value;
						return this;
					case 1:
						m11 = value;
						return this;
					case 2:
						m12 = value;
						return this;
				}
			case 2:
				switch (j) {
					case 0:
						m20 = value;
						return this;
					case 1:
						m21 = value;
						return this;
					case 2:
						m22 = value;
						return this;
				}
		}

		logger.warning("Invalid matrix index.");
		throw new IllegalArgumentException("Invalid indices into matrix.");
	}

	/**
	 * Recreate Matrix using the provided axis.
	 *
	 * @param uAxis  Vector3d
	 * @param vAxis  Vector3d
	 * @param wAxis  Vector3d
	 */
	public void fromAxes(Vector3d uAxis, Vector3d vAxis, Vector3d wAxis) {
		m00 = uAxis.x;
		m10 = uAxis.y;
		m20 = uAxis.z;

		m01 = vAxis.x;
		m11 = vAxis.y;
		m21 = vAxis.z;

		m02 = wAxis.x;
		m12 = wAxis.y;
		m22 = wAxis.z;
	}

	/**
	 * <code>set</code> defines the values of the matrix based on a supplied
	 * <code>Quaternion</code>. It should be noted that all previous values
	 * will be overridden.
	 *
	 * @param quaternion
	 *            the quaternion to create a rotational matrix from.
	 * @return this
	 */
	public Matrix3d set(Quaternion quaternion) {
		return quaternion.toRotationMatrix(this);
	}

	/**
	 * <code>loadIdentity</code> sets this matrix to the identity matrix.
	 * Where all values are zero except those along the diagonal which are one.
	 */
	public void loadIdentity() {
		m01 = m02 = m10 = m12 = m20 = m21 = 0;
		m00 = m11 = m22 = 1;
	}

	/**
	 * @return true if this matrix is identity
	 */
	public boolean isIdentity() {
		return (m00 == 1 && m01 == 0 && m02 == 0)
			   && (m10 == 0 && m11 == 1 && m12 == 0)
			   && (m20 == 0 && m21 == 0 && m22 == 1);
	}

	/**
	 * <code>mult</code> multiplies this matrix by a given matrix. The result
	 * matrix is returned as a new object.
	 *
	 * @param mat
	 *            the matrix to multiply this matrix by.
	 * @param product
	 *            the matrix to store the result in. if null, a new matrix3f is
	 *            created.  It is safe for mat and product to be the same object.
	 * @return a matrix3f object containing the result of this operation
	 */
	public Matrix3d mult(Matrix3d mat, Matrix3d product) {
		double temp00, temp01, temp02;
		double temp10, temp11, temp12;
		double temp20, temp21, temp22;

		if (product == null) {
			product = new Matrix3d();
		}
		temp00 = m00 * mat.m00 + m01 * mat.m10 + m02 * mat.m20;
		temp01 = m00 * mat.m01 + m01 * mat.m11 + m02 * mat.m21;
		temp02 = m00 * mat.m02 + m01 * mat.m12 + m02 * mat.m22;
		temp10 = m10 * mat.m00 + m11 * mat.m10 + m12 * mat.m20;
		temp11 = m10 * mat.m01 + m11 * mat.m11 + m12 * mat.m21;
		temp12 = m10 * mat.m02 + m11 * mat.m12 + m12 * mat.m22;
		temp20 = m20 * mat.m00 + m21 * mat.m10 + m22 * mat.m20;
		temp21 = m20 * mat.m01 + m21 * mat.m11 + m22 * mat.m21;
		temp22 = m20 * mat.m02 + m21 * mat.m12 + m22 * mat.m22;

		product.m00 = temp00;
		product.m01 = temp01;
		product.m02 = temp02;
		product.m10 = temp10;
		product.m11 = temp11;
		product.m12 = temp12;
		product.m20 = temp20;
		product.m21 = temp21;
		product.m22 = temp22;

		return product;
	}

	/**
	 * Multiplies this 3x3 matrix by the 1x3 Vector vec and stores the result in
	 * product.
	 *
	 * @param vec
	 *            The Vector3d to multiply.
	 * @param product
	 *            The Vector3d to store the result, it is safe for this to be
	 *            the same as vec.
	 * @return The given product vector.
	 */
	public Vector3d mult(Vector3d vec, Vector3d product) {
		if (null == product) {
			product = new Vector3d();
		}

		double x = vec.x;
		double y = vec.y;
		double z = vec.z;

		product.x = m00 * x + m01 * y + m02 * z;
		product.y = m10 * x + m11 * y + m12 * z;
		product.z = m20 * x + m21 * y + m22 * z;
		return product;
	}

	/**
	 * <code>multLocal</code> multiplies this matrix internally by
	 * a given double scale factor.
	 *
	 * @param scale
	 *            the value to scale by.
	 * @return this Matrix3d
	 */
	public Matrix3d multLocal(double scale) {
		m00 *= scale;
		m01 *= scale;
		m02 *= scale;
		m10 *= scale;
		m11 *= scale;
		m12 *= scale;
		m20 *= scale;
		m21 *= scale;
		m22 *= scale;
		return this;
	}

	/**
	 * Inverts this matrix and stores it in the given store.
	 *
	 * @param store storage for the result (modified if not null)
	 * @return The store
	 */
	public Matrix3d invert(Matrix3d store) {
		if (store == null) {
			store = new Matrix3d();
		}

		double det = determinant();
		if (Math.abs(det) <= 1.1920928955078125E-7) {
			return store.zero();
		}

		store.m00 = m11 * m22 - m12 * m21;
		store.m01 = m02 * m21 - m01 * m22;
		store.m02 = m01 * m12 - m02 * m11;
		store.m10 = m12 * m20 - m10 * m22;
		store.m11 = m00 * m22 - m02 * m20;
		store.m12 = m02 * m10 - m00 * m12;
		store.m20 = m10 * m21 - m11 * m20;
		store.m21 = m01 * m20 - m00 * m21;
		store.m22 = m00 * m11 - m01 * m10;

		store.multLocal(1f / det);
		return store;
	}

	/**
	 * <code>determinant</code> generates the determinant of this matrix.
	 *
	 * @return the determinant
	 */
	public double determinant() {
		double fCo00 = m11 * m22 - m12 * m21;
		double fCo10 = m12 * m20 - m10 * m22;
		double fCo20 = m10 * m21 - m11 * m20;
		double fDet = m00 * fCo00 + m01 * fCo10 + m02 * fCo20;
		return fDet;
	}

	/**
	 * Sets all of the values in this matrix to zero.
	 *
	 * @return this matrix
	 */
	public Matrix3d zero() {
		m00 = m01 = m02 = m10 = m11 = m12 = m20 = m21 = m22 = 0.0f;
		return this;
	}

	/**
	 * <code>toString</code> returns a string representation of this matrix.
	 * For example, an identity matrix would be represented by:
	 * <pre>
	 * Matrix3d
	 * [
	 *  1.0  0.0  0.0
	 *  0.0  1.0  0.0
	 *  0.0  0.0  1.0
	 * ]
	 * </pre>
	 *
	 * @return the string representation of this object.
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("Matrix3d\n[\n");
		result.append(" ");
		result.append(m00);
		result.append("  ");
		result.append(m01);
		result.append("  ");
		result.append(m02);
		result.append(" \n");
		result.append(" ");
		result.append(m10);
		result.append("  ");
		result.append(m11);
		result.append("  ");
		result.append(m12);
		result.append(" \n");
		result.append(" ");
		result.append(m20);
		result.append("  ");
		result.append(m21);
		result.append("  ");
		result.append(m22);
		result.append(" \n]");
		return result.toString();
	}

	/**
	 * <code>hashCode</code> returns the hash code value as an integer and is
	 * supported for the benefit of hashing based collection classes such as
	 * Hashtable, HashMap, HashSet etc.
	 *
	 * @return the hashcode for this instance of Matrix4f.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 37;
		hash = (int) (37 * hash + Double.doubleToLongBits(m00));
		hash = (int) (37 * hash + Double.doubleToLongBits(m01));
		hash = (int) (37 * hash + Double.doubleToLongBits(m02));

		hash = (int) (37 * hash + Double.doubleToLongBits(m10));
		hash = (int) (37 * hash + Double.doubleToLongBits(m11));
		hash = (int) (37 * hash + Double.doubleToLongBits(m12));

		hash = (int) (37 * hash + Double.doubleToLongBits(m20));
		hash = (int) (37 * hash + Double.doubleToLongBits(m21));
		hash = (int) (37 * hash + Double.doubleToLongBits(m22));

		return hash;
	}

	/**
	 * are these two matrices the same? they are is they both have the same mXX values.
	 *
	 * @param o   the object to compare for equality
	 * @return true if they are equal
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null || o.getClass() != getClass()) {
			return false;
		}

		if (this == o) {
			return true;
		}

		Matrix3d comp = (Matrix3d) o;
		if (Double.compare(m00, comp.m00) != 0) {
			return false;
		}
		if (Double.compare(m01, comp.m01) != 0) {
			return false;
		}
		if (Double.compare(m02, comp.m02) != 0) {
			return false;
		}

		if (Double.compare(m10, comp.m10) != 0) {
			return false;
		}
		if (Double.compare(m11, comp.m11) != 0) {
			return false;
		}
		if (Double.compare(m12, comp.m12) != 0) {
			return false;
		}

		if (Double.compare(m20, comp.m20) != 0) {
			return false;
		}
		if (Double.compare(m21, comp.m21) != 0) {
			return false;
		}
		if (Double.compare(m22, comp.m22) != 0) {
			return false;
		}

		return true;
	}

	/**
	 * Create a copy of this matrix.
	 *
	 * @return a new instance, equivalent to this one
	 */
	@Override
	public Matrix3d clone() {
		try {
			return (Matrix3d) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(); // can not happen
		}
	}
}
