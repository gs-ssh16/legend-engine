# Apache Calcite SQL optimizer in Legend

[Apache Calcite](https://calcite.apache.org/) is an open-source data management framework that provides a robust and extensible platform for building custom data management and query processing solutions. As a versatile and pluggable tool, Calcite supports diverse use cases, including query parsing, validation, and optimization. It functions as a bridge between applications and various data storage systems, offering a unified interface for query generation and execution across different data sources.

A key element of the Apache Calcite framework is it's SQL optimizer/planner component which we aim to utilize in the Legend platform for optimizing SQL queries. In Calcite's terminology, a RelOptPlanner functions as a query optimizer, reshaping a relational expression into a semantically equivalent form based on a specified set of rules (or/and) a defined cost model. Some key advantages of the Calcite optimizer framework include:

- Pluggable architecture, allowing extension with custom rules and strategies
- Rule based and cost based optimization
- Rich set of ready to use optimization rules contributed by open source community

## Calcite optimizer integration strategy

The proposed approach is to integrate Calcite optimizer into Legend platform as a post-processor for SQL queries, in the SQL optimization stage described [here](../QueryOptimization.md). Few decisions must be made in this regard:

- Conversion from Legend SQL model to Calcite SQL model: Can be defined as a model-to-model (M2M) transformation between the two, or can the industry-standard SQL parser available in Calcite be leveraged? Presently, inclined towards implementing an M2M transform.

- SQL string conversion of optimized query: Utilize SQLDialects defined in Calcite for transforming the optimized query into an executable SQL string, or should we consider coding a reverse M2M transformation from the Calcite SQL model back to the Legend SQL model and continue from there. Check dialect support comparison table below.


## Legend, Calcite SQL dialect support comparison



| DatabseType    | Legend             | Calcite            |
| -----------    | ------             | -------            |
| Snowflake      | :white_check_mark: | :white_check_mark: |
| Redshift       | :white_check_mark: | :white_check_mark: |
| BigQuery       | :white_check_mark: | :white_check_mark: |
| H2             | :white_check_mark: | :white_check_mark: |
| DB2            | :white_check_mark: | :white_check_mark: |
| MemSQL/MySQL   | :white_check_mark: | :white_check_mark: |
| Postgres       | :white_check_mark: | :white_check_mark: |
| SqlServer      | :white_check_mark: | :white_check_mark: |
| Presto         | :white_check_mark: | :white_check_mark: |
| Hive           | :white_check_mark: | :white_check_mark: |
| Sybase         | :white_check_mark: | :white_check_mark: |
| SparkSQL       | :white_check_mark: | :white_check_mark: |
| Athena         | :white_check_mark: | :x:                |
| Databricks     | :white_check_mark: | :x:                |
| Trino          | :white_check_mark: | :x: (Presto?)      |
| Spanner        | :white_check_mark: | :x: (BigQuery?)    |
| SybaseIQ       | :white_check_mark: | :x: (Sybase?)      |
| Access         | :x:                | :white_check_mark: |
| Calcite        | :x:                | :white_check_mark: |
| Clickhouse     | :x:                | :white_check_mark: |
| Derby          | :x:                | :white_check_mark: |
| Exasol         | :x:                | :white_check_mark: |
| Firebird       | :x:                | :white_check_mark: |
| Firebolt       | :x:                | :white_check_mark: |
| HsqlDb         | :x:                | :white_check_mark: |
| Infobright     | :x:                | :white_check_mark: |
| Informix       | :x:                | :white_check_mark: |
| Ingres         | :x:                | :white_check_mark: |
| Interbase      | :x:                | :white_check_mark: |
| JethroData     | :x:                | :white_check_mark: |
| LucidDb        | :x:                | :white_check_mark: |
| Neoview        | :x:                | :white_check_mark: |
| Netezza        | :x:                | :white_check_mark: |
| Orcale         | :x:                | :white_check_mark: |
| Paraccel       | :x:                | :white_check_mark: |
| Phoneix        | :x:                | :white_check_mark: |
| Teradata       | :x:                | :white_check_mark: |
| Vertica        | :x:                | :white_check_mark: |


## Optimizations

### Unused field trimming

<table>
<tr>
<th>Input SQL</th>
<th>Optimized SQL</th>
</tr>
<tr>
<td>

```sql

SELECT
  unionBase."name" AS "name"
FROM
  (
    SELECT
      root.ID AS "pk_0_0",
      NULL AS "pk_0_1",
      root.lastName_s1 AS "name"
    FROM
      PersonSet1 AS root
    UNION ALL
    SELECT
      NULL AS "pk_0_0",
      root.ID AS "pk_0_1",
      root.lastName_s2 AS "name"
    FROM
      PersonSet2 AS root
  ) AS unionBase
```

</td>
<td>

    
```sql
SELECT
    *
FROM
    (
        SELECT
            lastName_s1 AS "name"
        FROM
            PersonSet1
        UNION ALL
        SELECT
            lastName_s2 AS "name"
        FROM
            PersonSet2
    )
```

</td>
</tr>
</table>


### Filter push down, left join to inner join conversion

<table>
<tr>
<th>Input SQL</th>
<th>Optimized SQL</th>
</tr>
<tr>
<td>


```sql

SELECT
  root.id AS "pk_0",
  root.name AS "name"
FROM
  orgTable AS root
  LEFT JOIN otherTable AS othertable_0
   ON root.id = othertable_0.orgTableId
WHERE
  othertable_0.filterVal <= 4
  AND root.name = 'Firm X'
```

</td>
<td>

    
```sql
SELECT
    t.id AS "pk_0",
    t.name AS "name"
FROM
    (
        SELECT
            id,
            name
        FROM
            orgTable
        WHERE
            name = 'Firm X'
    ) AS t
    INNER JOIN (
        SELECT
            *
        FROM
            otherTable
        WHERE
            filterVal <= 4
    ) AS t0 ON t.id = t0.orgTableId
```

</td>
</tr>
</table>


### Join condition push

<table>
<tr>
<th>Input SQL</th>
<th>Optimized SQL</th>
</tr>
<tr>
<td>


```sql
SELECT
  producttable_0.name AS "Prod Name"
FROM
  OrderTable AS root
  LEFT JOIN ProductTable AS producttable_0
   ON root.prodFk = producttable_0.id
      AND (
        producttable_0.from_z <= CAST('2015-08-25' AS DATE)
        AND producttable_0.thru_z > CAST('2015-08-25' AS DATE)
      )
```

</td>
<td>

    
```sql
SELECT
    t2.name AS "Prod Name"
FROM
    (
        SELECT
            prodFk
        FROM
            OrderTable
        ORDER BY
            prodFk
    ) AS t0
    LEFT JOIN (
        SELECT
            id,
            name,
            from_z,
            thru_z
        FROM
            ProductTable
        WHERE
            from_z <= DATE '2015-08-25'
            AND thru_z > DATE '2015-08-25'
        ORDER BY
            id
    ) AS t2 ON t0.prodFk = t2.id
```

</td>
</tr>
</table>



### Distinct to Group By conversion

<table>
<tr>
<th>Input SQL</th>
<th>Optimized SQL</th>
</tr>
<tr>
<td>


```sql
SELECT DISTINCT
  root.FIRSTNAME AS "firstName",
  root.LASTNAME AS "lastName"
FROM
  personTable AS root
```

</td>
<td>

    
```sql
SELECT
    FIRSTNAME,
    LASTNAME
FROM
    personTable
GROUP BY
    FIRSTNAME,
    LASTNAME
```

</td>
</tr>
</table>
