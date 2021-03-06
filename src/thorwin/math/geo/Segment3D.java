/******************************************************************************
 * Copyright (C) 2015 Sebastiaan R. Hogenbirk                                 *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU Lesser General Public License as published by*
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU Lesser General Public License for more details.                        *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public License   *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package thorwin.math.geo;

import java.io.Serializable;
import java.util.Optional;
import java.util.OptionalDouble;

import thorwin.math.Vector3D;

/**
 * Line segment between two points in 3-dimensional space.
 */
public final class Segment3D implements Serializable {

  private static final long serialVersionUID = 6704863245421525870L;

  private final double x1;
  private final double y1;
  private final double z1;
  private final double x2;
  private final double y2;
  private final double z2;

  /**
   * Constructs a segment between two points.
   *
   * @param p1 point 1
   * @param p2 point 2
   */
  public Segment3D(Vector3D p1, Vector3D p2) {
    this(p1.getX(), p1.getY(), p1.getZ(), p2.getX(), p2.getY(), p2.getZ());
  }

  /**
   * Constructs a segment between two points.
   *
   * @param x1 x-coordinate of point 1
   * @param y1 y-coordinate of point 1
   * @param z1 z-coordinate of point 1
   * @param x2 x-coordinate of point 2
   * @param y2 y-coordinate of point 2
   * @param z2 z-coordinate of point 2
   */
  public Segment3D(double x1,
                   double y1,
                   double z1,
                   double x2,
                   double y2,
                   double z2) {
    super();
    this.x1 = x1;
    this.y1 = y1;
    this.z1 = z1;
    this.x2 = x2;
    this.y2 = y2;
    this.z2 = z2;
  }

  /**
   * Flips the two reference points <i>P1</i> and <i>P2</i>
   *
   * @return new segment
   */
  public Segment3D flip() {
    return new Segment3D(x2, y2, z2, x1, y2, z1);
  }

  /**
   * Returns the x-coordinate of reference point <i>P1</i>
   * @return <i>P1.x</i>
   */
  public double getX1() {
    return x1;
  }

  /**
   * Returns the x-coordinate of reference point <i>P2</i>
   * @return <i>P2.x</i>
   */
  public double getX2() {
    return x2;
  }

  /**
   * Returns the y-coordinate of reference point <i>P1</i>
   * @return <i>P1.y</i>
   */
  public double getY1() {
    return y1;
  }

  /**
   * Returns the y-coordinate of reference point <i>P2</i>
   * @return <i>P2.y</i>
   */
  public double getY2() {
    return y2;
  }

  /**
   * Returns the z-coordinate of reference point <i>P1</i>
   * @return <i>P1.z</i>
   */
  public double getZ1() {
    return z1;
  }

  /**
   * Returns the z-coordinate of reference point <i>P2</i>
   * @return <i>P2.z</i>
   */
  public double getZ2() {
    return z2;
  }

  /**
   * Transform this line segment
   *
   * @param transformation transformation
   * @return transformed line segment
   */
  public Segment3D transform(Transform3D transformation) {
    return new Segment3D(
        transformation.transform(getP1()),
        transformation.transform(getP2())
    );
  }

  /**
   * Returns reference point <i>P1</i>
   * @return <i>P1</i>
   */
  public Vector3D getP1() {
    return new Vector3D(x1, y1, z1);
  }

  /**
   * Returns reference point <i>P2</i>
   * @return <i>P2</i>
   */
  public Vector3D getP2() {
    return new Vector3D(x2, y2, z2);
  }

  /**
   * The origin of this line (same as <i>P1</i>) in an origin/direction notation of
   * this line.
   *
   * @return origin point
   */
  public Vector3D getOrigin() {
    return getP1();
  }

  /**
   * Returns the direction of this line (<i>P2-P1</i>) in an origin/direction
   * notation of this line.
   *
   * @return normalized vector
   */
  public Vector3D getDirection() {
    return getP2().subtract(getP1()).normalize();
  }

  /**
   * Calculates the intersection distance from <i>P1</i> on the line.
   *
   * @param plane plane
   * @return intersection distance
   */
  public OptionalDouble intersectionDistance(Plane3D plane) {
    Vector3D n = plane.getNormal();
    Vector3D p0 = plane.getOrigin();
    Vector3D l = getDirection();
    Vector3D l0 = getOrigin();

    double d =  l.multiply(n);

    if (d == 0) {
     return OptionalDouble.empty();
    } else {
      return OptionalDouble.of(p0.subtract(l0).multiply(n) / d);
    }
  }

  /**
   * Returns the intersection point between this line segment and a plane, if any
   * @param plane plane
   * @return the intersection point, if any
   */
  public Optional<Vector3D> intersection(Plane3D plane) {
    OptionalDouble distance = intersectionDistance(plane);

    // parallel or behind origin
    if (!distance.isPresent() || (distance.getAsDouble() < 0) || (distance.getAsDouble() > getP1()
        .subtract(
            getP2()).length()))
      return Optional.empty();

    return Optional.of(
        getOrigin().add(getDirection().normalize().multiply(distance.getAsDouble()))
    );
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(x1);
    result = (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(y1);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(z1);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(x2);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(y2);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(z2);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if ((obj == null) || (getClass() != obj.getClass())) return false;

    Segment3D segment3D = (Segment3D) obj;

    return Double.compare(segment3D.x1, x1) == 0 && Double.compare(segment3D.x2,
                                                                   x2) == 0
        && Double.compare(
        segment3D.y1,
        y1) == 0 && Double.compare(segment3D.y2, y2) == 0 && Double.compare(
        segment3D.z1,
        z1) == 0 && Double.compare(segment3D.z2, z2) == 0;

  }

  @Override
  public String toString() {
    return "Segment3D{" + "x1=" + x1 + ", y1=" + y1 + ", z1=" + z1 + ", x2=" + x2 + ", y2=" + y2 + ", " +
        "z2=" + z2 + '}';
  }
}
