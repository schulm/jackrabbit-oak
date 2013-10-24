/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.query.xpath;

import java.util.ArrayList;

import org.apache.jackrabbit.oak.query.QueryImpl;
import org.apache.jackrabbit.oak.query.xpath.Expression.Property;

/**
 * An xpath statement.
 */
public class Statement {

    private String xpathQuery;
    
    private boolean explain;
    private boolean measure;
    
    /**
     * The selector to get the columns from (the selector used in the select
     * column list).
     */
    private Selector columnSelector;
    
    private ArrayList<Expression> columnList = new ArrayList<Expression>();
    
    /**
     * All selectors.
     */
    private ArrayList<Selector> selectors;

    private ArrayList<Order> orderList = new ArrayList<Order>();
    
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        
        // explain | measure ...
        if (explain) {
            buff.append("explain ");
        } else if (measure) {
            buff.append("measure ");
        }
        
        // select ...
        buff.append("select ");
        buff.append(new Expression.Property(columnSelector, QueryImpl.JCR_PATH).toString());
        if (selectors.size() > 1) {
            buff.append(" as ").append('[').append(QueryImpl.JCR_PATH).append(']');
        }
        buff.append(", ");
        buff.append(new Expression.Property(columnSelector, QueryImpl.JCR_SCORE).toString());
        if (selectors.size() > 1) {
            buff.append(" as ").append('[').append(QueryImpl.JCR_SCORE).append(']');
        }
        if (columnList.isEmpty()) {
            buff.append(", ");
            buff.append(new Expression.Property(columnSelector, "*").toString());
        } else {
            for (int i = 0; i < columnList.size(); i++) {
                buff.append(", ");
                Expression e = columnList.get(i);
                String columnName = e.toString();
                buff.append(columnName);
                if (selectors.size() > 1) {
                    buff.append(" as [").append(e.getColumnAliasName()).append("]");
                }
            }
        }
        
        // from ...
        buff.append(" from ");
        for (int i = 0; i < selectors.size(); i++) {
            Selector s = selectors.get(i);
            if (i > 0) {
                buff.append(" inner join ");
            }
            String nodeType = s.nodeType;
            if (nodeType == null) {
                nodeType = "nt:base";
            }
            buff.append('[' + nodeType + ']').append(" as ").append(s.name);
            if (s.joinCondition != null) {
                buff.append(" on ").append(s.joinCondition);
            }
        }
        
        // where ...
        Expression where = null;
        for (Selector s : selectors) {
            where = Expression.and(where, s.condition);
        }
        if (where != null) {
            buff.append(" where ").append(where.toString());
        }
        
        // order by ...
        if (!orderList.isEmpty()) {
            buff.append(" order by ");
            for (int i = 0; i < orderList.size(); i++) {
                if (i > 0) {
                    buff.append(", ");
                }
                buff.append(orderList.get(i));
            }
        }

        // leave original xpath string as a comment
        buff.append(" /* xpath: ");
        buff.append(xpathQuery);
        buff.append(" */");
        return buff.toString();        
    }

    public void setExplain(boolean explain) {
        this.explain = explain;
    }

    public void setMeasure(boolean measure) {
        this.measure = measure;
    }

    public void addSelectColumn(Property p) {
        columnList.add(p);
    }

    public void setSelectors(ArrayList<Selector> selectors) {
        this.selectors = selectors;
    }

    public void addOrderBy(Order order) {
        this.orderList.add(order);
    }

    public void setColumnSelector(Selector columnSelector) {
        this.columnSelector = columnSelector;
    }
    
    public void setOriginalQuery(String xpathQuery) {
        this.xpathQuery = xpathQuery;
    }

}
