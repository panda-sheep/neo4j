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
package org.neo4j.cypher.internal.compiler.v4_0.planner

import org.mockito.Mockito._
import org.neo4j.cypher.internal.planner.v4_0.spi.PlanContext
import org.neo4j.cypher.internal.v4_0.ast.semantics.SemanticTable
import org.neo4j.cypher.internal.v4_0.ast.{Match, Query, SingleQuery, Where, _}
import org.neo4j.cypher.internal.v4_0.expressions._
import org.neo4j.cypher.internal.v4_0.util.test_helpers.CypherFunSuite
import org.neo4j.cypher.internal.v4_0.util.{LabelId, PropertyKeyId, RelTypeId}

class ResolveTokensTest extends CypherFunSuite {

  import org.neo4j.cypher.internal.v4_0.parser.ParserFixture._

  parseTest("match (n) where n.name = 'Resolved' return *") { query =>
    implicit val semanticTable = SemanticTable()
    val planContext = mock[PlanContext]
    when(planContext.getOptPropertyKeyId("name")).thenReturn(Some(12))

    ResolveTokens.resolve(query)(semanticTable, planContext)

    query match {
      case Query(_,
        SingleQuery(Seq(
          Match(
            false,
            Pattern(Seq(EveryPath(NodePattern(Some(Variable("n")), Seq(), None, _)))),
            Seq(),
            Some(Where(Equals(Property(Variable("n"), pkToken), StringLiteral("Resolved"))))
          ),
          Return(false, ReturnItems(true, Seq()), None, None, None, _)
        ))) =>
            pkToken.name should equal("name")
            semanticTable.id(pkToken) should equal(Some(PropertyKeyId(12)))
    }
  }

  parseTest("match (n) where n.name = 'Unresolved' return *") { query =>
    implicit val semanticTable = SemanticTable()
    val planContext = mock[PlanContext]
    when(planContext.getOptPropertyKeyId("name")).thenReturn(None)

    ResolveTokens.resolve(query)(semanticTable, planContext)

    query match {
      case Query(_,
        SingleQuery(Seq(
          Match(
            false,
            Pattern(Seq(EveryPath(NodePattern(Some(Variable("n")), Seq(), None, _)))),
            Seq(),
            Some(Where(Equals(Property(Variable("n"), pkToken), StringLiteral("Unresolved"))))
          ),
          Return(false, ReturnItems(true, Seq()), None, None, None, _)
        ))) =>
            pkToken.name should equal("name")
            semanticTable.id(pkToken) should equal(None)
    }
  }

  parseTest("match (n) where n:Resolved return *") { query =>
    implicit val semanticTable = SemanticTable()
    val planContext = mock[PlanContext]
    when(planContext.getOptLabelId("Resolved")).thenReturn(Some(12))

    ResolveTokens.resolve(query)(semanticTable, planContext)

    query match {
      case Query(_,
      SingleQuery(Seq(
        Match(
          false,
          Pattern(Seq(EveryPath(NodePattern(Some(Variable("n")), Seq(), None, _)))),
          Seq(),
          Some(Where(HasLabels(Variable("n"), Seq(labelToken))))
        ),
        Return(false, ReturnItems(true, Seq()), None, None, None, _)
      ))) =>
        labelToken.name should equal("Resolved")
        semanticTable.id(labelToken) should equal(Some(LabelId(12)))
    }
  }

  parseTest("match (n) where n:Unresolved return *") { query =>
    implicit val semanticTable = SemanticTable()
    val planContext = mock[PlanContext]
    when(planContext.getOptLabelId("Unresolved")).thenReturn(None)

    ResolveTokens.resolve(query)(semanticTable, planContext)

    query match {
      case Query(_,
      SingleQuery(Seq(
        Match(
          false,
          Pattern(Seq(EveryPath(NodePattern(Some(Variable("n")), Seq(), None, _)))),
          Seq(),
          Some(Where(HasLabels(Variable("n"), Seq(labelToken))))
        ),
        Return(false, ReturnItems(true, Seq()), None, None, None, _)
      ))) =>
        labelToken.name should equal("Unresolved")
        semanticTable.id(labelToken) should equal(None)
    }
  }

  parseTest("match ()-[:RESOLVED]->() return *") { query =>
    implicit val semanticTable = SemanticTable()
    val planContext = mock[PlanContext]
    when(planContext.getOptRelTypeId("RESOLVED")).thenReturn(Some(12))

    ResolveTokens.resolve(query)(semanticTable, planContext)

    query match {
      case Query(_,
      SingleQuery(Seq(
        Match(
          false,
          Pattern(Seq(EveryPath(RelationshipChain(
            NodePattern(None, Seq(), None, _),
            RelationshipPattern(None, Seq(relTypeToken), None, None, SemanticDirection.OUTGOING, _, _),
            NodePattern(None, Seq(), None, _)
          )))),
          Seq(),
          None
        ),
        Return(false, ReturnItems(true, Seq()), None, None, None, _)
      ))) =>
        relTypeToken.name should equal("RESOLVED")
        semanticTable.id(relTypeToken) should equal(Some(RelTypeId(12)))
    }
  }

  parseTest("match ()-[:UNRESOLVED]->() return *") { query =>
    implicit val semanticTable = SemanticTable()
    val planContext = mock[PlanContext]
    when(planContext.getOptRelTypeId("UNRESOLVED")).thenReturn(None)

    ResolveTokens.resolve(query)(semanticTable, planContext)

    query match {
      case Query(_,
      SingleQuery(Seq(
        Match(
          false,
          Pattern(Seq(EveryPath(RelationshipChain(
            NodePattern(None, Seq(), None, _),
            RelationshipPattern(None, Seq(relTypeToken), None, None, SemanticDirection.OUTGOING, _, _),
            NodePattern(None, Seq(), None, _)
          )))),
          Seq(),
          None
        ),
        Return(false, ReturnItems(true, Seq()), None, None, None, _)
      ))) =>
        relTypeToken.name should equal("UNRESOLVED")
        semanticTable.id(relTypeToken) should equal(None)
    }
  }

  def parseTest(queryText: String)(f: Query => Unit) = test(queryText) { parser.parse(queryText) match {
    case query: Query => f(query)
    }
  }
}
