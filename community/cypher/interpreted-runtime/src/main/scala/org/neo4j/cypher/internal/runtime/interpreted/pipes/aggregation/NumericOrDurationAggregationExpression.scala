/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.runtime.interpreted.pipes.aggregation

import org.neo4j.cypher.internal.runtime.interpreted.commands.expressions.Expression
import org.neo4j.values.AnyValue
import org.neo4j.values.storable.{DurationValue, NumberValue, Values}
import org.neo4j.cypher.internal.v4_0.util.CypherTypeException

trait NumericOrDurationAggregationExpression {
  trait AggregatingType
  case object AggregatingNumbers extends AggregatingType
  case object AggregatingDurations extends AggregatingType

  protected var sumNumber: NumberValue = Values.ZERO_INT
  protected var sumDuration: DurationValue = DurationValue.ZERO
  protected var aggregatingType: Option[AggregatingType] = None

  def name: String

  def value: Expression

  protected def actOnNumberOrDuration(vl: AnyValue, aggNumber: NumberValue => Unit, aggDuration: DurationValue => Unit) = {
    vl match {
      case Values.NO_VALUE =>
      case number: NumberValue =>
        aggregatingType match {
          case None =>
            aggregatingType = Some(AggregatingNumbers)
          case Some(AggregatingDurations) =>
            throw new CypherTypeException("%s(%s) cannot mix number and durations".format(name, value))
          case _ =>
        }
        aggNumber(number)
      case dur: DurationValue =>
        aggregatingType match {
          case None =>
            aggregatingType = Some(AggregatingDurations)
          case Some(AggregatingNumbers) =>
            throw new CypherTypeException("%s(%s) cannot mix number and durations".format(name, value))
          case _ =>
        }
        aggDuration(dur)
      case _ =>
        throw new CypherTypeException("%s(%s) can only handle numerical values, duration, or null.".format(name, value))
    }
  }
}
