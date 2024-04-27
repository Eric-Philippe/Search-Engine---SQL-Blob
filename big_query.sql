-- Clients avec le projet le plus haut 
WITH CLIENTS_PJ AS (
    SELECT 'CLIENT_1' AS clientid, 'PROJET_1' AS projetid FROM DUAL
    UNION ALL
    SELECT 'CLIENT_1', 'PROJET_2' FROM DUAL
    UNION ALL
    SELECT 'CLIENT_2', 'PROJET_1' FROM DUAL
    UNION ALL
    SELECT 'CLIENT_2', 'PROJET_2' FROM DUAL
    UNION ALL
    SELECT 'CLIENT_3', 'PROJET_1' FROM DUAL
)
-- SELECT * FROM CLIENTS_PJ;


,
-- Clients lié uniquement à BC 
CLIENTS_BC AS (
    SELECT 'CLIENT_1' AS clientid, 'PROJET_ID' AS projetid FROM DUAL
    UNION ALL
    SELECT 'CLIENT_2', 'PROJET_ID' FROM DUAL
    UNION ALL
    SELECT 'CLIENT_3', 'PROJET_ID' FROM DUAL
),
-- SELECT * FROM CLIENTS_BC


PHASES AS (
  SELECT 'C' AS phase FROM dual UNION ALL 
  SELECT 'F' AS phase FROM dual UNION ALL 
  SELECT 'P' AS phase FROM dual UNION ALL 
  SELECT 'D' AS phase FROM dual UNION ALL 
  SELECT 'S' AS phase FROM dual 
)
-- SELECT * FROM PHASES;


,
APPLE_RUBRIQUES_CLIENTS_PROJET AS (
    SELECT modeleid, CLI.clientid, CLI.projetid, rubid FROM (
        SELECT DISTINCT
        FIRST_VALUE(modeleid) OVER (PARTITION BY rubid ORDER BY CASE WHEN modeleid = 'MODELE_ID' THEN 0 ELSE 1 END) AS modeleid,
        clientid, projetid, rubid FROM APPLE_REGLE_RUBRIQUE
        CROSS JOIN (SELECT DISTINCT clientid FROM CLIENTS_BC)
        WHERE versionid = 'VERSION_ID'
        AND projetid = 'PROJET_ID'
        AND modeleid IN ('MODELE_ID', 'MODELE_ID_2')
     ) CROSS JOIN CLIENTS_PJ CLI
)
-- SELECT * FROM APPLE_RUBRIQUES_CLIENTS_PROJET;

,
PEAR_RUBRIQUES_CLIENTS_PROJET AS (
SELECT * FROM (
    SELECT clipj.clientid, clipj.projetid, rubid FROM CLIENTS_PJ clipj
    CROSS JOIN (
        SELECT DISTINCT clientid, projetid, rubid FROM PEAR_REGLE_RUBRIQUE
        WHERE (clientid, projetid) IN (SELECT * FROM CLIENTS_BC)
    )
    UNION ALL
    SELECT DISTINCT clientid, projetid, rubid FROM PEAR_REGLE_RUBRIQUE
    WHERE (clientid, projetid) IN (SELECT * FROM CLIENTS_PJ)
    )
    --ORDER BY clientid, projetid, rubid
)
-- SELECT * FROM PEAR_RUBRIQUES_CLIENTS_PROJET;


,
RUBRIQUES AS (
    SELECT DISTINCT clientid, projetid, rubid
    FROM (
        SELECT clientid, projetid, rubid FROM PEAR_RUBRIQUES_CLIENTS_PROJET
        UNION ALL
        SELECT clientid, projetid, rubid FROM APPLE_RUBRIQUES_CLIENTS_PROJET
    )
    --ORDER BY clientid, projetid, rubid
)
--SELECT * FROM RUBRIQUES;

, 
RUBRIQUES_INFOS AS (
    SELECT RUBRIQUES.clientid, RUBRIQUES.projetid, plpear.rubid, plpear.objetid, 
    CASE WHEN plpear.gestcot IS NULL THEN '0' ELSE plpear.gestcot END AS gestcot
    FROM pear_lien_applerubzadig plpear 
    INNER JOIN RUBRIQUES ON plpear.rubid = RUBRIQUES.rubid 
    WHERE  plpear.clientid = RUBRIQUES.clientid 
    UNION 
    SELECT RUBRIQUES.clientid, RUBRIQUES.projetid, plapple.rubid, plapple.objetid, plapple.gestcot 
    FROM apple_lien_applerubzadig plapple
    INNER JOIN RUBRIQUES ON plapple.rubid = RUBRIQUES.rubid 
    AND (
      plapple.modeleid, plapple.versionid, 
      plapple.projetid
    ) IN (
      ('MODELE_ID', 'VERSION_ID', 'PROJET_ID'), 
      ('MODELE_ID_1', 'VERSION_ID_1', 'PROJET_ID')
    )
)
-- SELECT * FROM RUBRIQUES_INFOS;

, 
RUBRIQUES_FULL AS (
    SELECT RI.*,
    CASE
        WHEN SUBSTR(objetid, 1, 2) = 'Z8' THEN (
            COALESCE (
                (SELECT libelle FROM PEAR_REFERENTIEL WHERE (clientid, projetid, objetid) IN ((RI.clientid, RI.projetid, RI.objetid))),
                (SELECT libelle FROM PEAR_REFERENTIEL WHERE (clientid, projetid, objetid) IN ((RI.clientid, 'PROJET_ID', RI.objetid)))
            )
        )
        ELSE (
            COALESCE (
                (SELECT ecartvaleur FROM PEAR_REFERENTIEL_E WHERE (clientid, projetid, objetid, typeobjet, ecartattribut) IN ((RI.clientid, RI.projetid, RI.objetid, 'RUB', 'LIBELLE'))),
                (SELECT ecartvaleur FROM PEAR_REFERENTIEL_E WHERE (clientid, projetid, objetid, typeobjet, ecartattribut) IN ((RI.clientid, 'PROJET_ID', RI.objetid, 'RUB', 'LIBELLE'))),
                (SELECT libelle FROM APPLE_REFERENTIEL_RES WHERE (modeleid, versionid, projetid, objetid, typeobjet) IN (('MODELE_ID', 'VERSION_ID', 'PROJET_ID', RI.objetid, 'RUB')))
            )
        )
    END AS libelle,
    
    CASE
        WHEN SUBSTR(objetid, 1, 2) = 'Z8' THEN (
            COALESCE (
                (SELECT option_retro FROM PEAR_TABLE_RUBRIQUE_CALCUL WHERE (clientid, projetid, rubid) IN ((RI.clientid, RI.projetid, RI.rubid))),
                (SELECT option_retro FROM PEAR_TABLE_RUBRIQUE_CALCUL  WHERE (clientid, projetid, rubid) IN ((RI.clientid, 'PROJET_ID', RI.rubid)))
            ) 
        )
        WHEN SUBSTR(objetid, 1, 2) = 'Z4' THEN (
        COALESCE(
                (SELECT ecartvaleur FROM PEAR_TABLE_RUBRIQUE_CALCUL_E WHERE (clientid, projetid, rubid) IN ((RI.clientid, RI.projetid, RI.rubid)) AND ecartattribut = 'OPTION_RETRO'),
                (SELECT ecartvaleur FROM PEAR_TABLE_RUBRIQUE_CALCUL_E WHERE (clientid, projetid, rubid) IN ((RI.clientid, 'PROJET_ID', RI.rubid)) AND ecartattribut = 'OPTION_RETRO'),
                (SELECT option_retro FROM APPLE_TABLE_RUBRIQUE_CALCUL WHERE (modeleid, versionid, projetid, rubid) IN (('MODELE_ID', 'VERSION_ID', 'PROJET_ID', RI.rubid)))
            )
        )
        ELSE (
            COALESCE(
                    (SELECT ecartvaleur FROM PEAR_TABLE_RUBRIQUE_CALCUL_E WHERE (clientid, projetid, rubid) IN ((RI.clientid, RI.projetid, RI.rubid)) AND ecartattribut = 'OPTION_RETRO'),
                    (SELECT ecartvaleur FROM PEAR_TABLE_RUBRIQUE_CALCUL_E WHERE (clientid, projetid, rubid) IN ((RI.clientid, 'PROJET_ID', RI.rubid)) AND ecartattribut = 'OPTION_RETRO'),
                    (SELECT valecartval FROM APPLE_ECART_VALCOL_RUB WHERE (modeleid, versionid, projetid, rubid) IN (('MODELE_ID', 'VERSION_ID', 'PROJET_ID', RI.rubid)) AND (tablerubid, colonneid) IN (('APPLE_TABLE_RUBRIQUE_CALCUL', 'OPTION_RETRO'))),
                    (SELECT option_retro FROM APPLE_TABLE_RUBRIQUE_CALCUL WHERE (modeleid, versionid, projetid, rubid) IN (('MODELE_ID_1', 'VERSION_ID', 'PROJET_ID', RI.rubid)))
                )
            )
    END AS option_retro,
    
        CASE
        WHEN SUBSTR(objetid, 1, 2) = 'Z8' THEN (
            COALESCE (
                (SELECT option_cscp FROM PEAR_TABLE_RUBRIQUE_CALCUL WHERE (clientid, projetid, rubid) IN ((RI.clientid, RI.projetid, RI.rubid))),
                (SELECT option_cscp FROM PEAR_TABLE_RUBRIQUE_CALCUL  WHERE (clientid, projetid, rubid) IN ((RI.clientid, 'PROJET_ID', RI.rubid)))
            ) 
        )
        WHEN SUBSTR(objetid, 1, 2) = 'Z4' THEN (
        COALESCE(
                (SELECT ecartvaleur FROM PEAR_TABLE_RUBRIQUE_CALCUL_E WHERE (clientid, projetid, rubid) IN ((RI.clientid, RI.projetid, RI.rubid)) AND ecartattribut = 'OPTION_CSCP'),
                (SELECT ecartvaleur FROM PEAR_TABLE_RUBRIQUE_CALCUL_E WHERE (clientid, projetid, rubid) IN ((RI.clientid, 'PROJET_ID', RI.rubid)) AND ecartattribut = 'OPTION_CSCP'),
                (SELECT option_retro FROM APPLE_TABLE_RUBRIQUE_CALCUL WHERE (modeleid, versionid, projetid, rubid) IN (('MODELE_ID', 'VERSION_ID', 'PROJET_ID', RI.rubid)))
            )
        )
        ELSE (
            COALESCE(
                    (SELECT ecartvaleur FROM PEAR_TABLE_RUBRIQUE_CALCUL_E WHERE (clientid, projetid, rubid) IN ((RI.clientid, RI.projetid, RI.rubid)) AND ecartattribut = 'OPTION_CSCP'),
                    (SELECT ecartvaleur FROM PEAR_TABLE_RUBRIQUE_CALCUL_E WHERE (clientid, projetid, rubid) IN ((RI.clientid, 'PROJET_ID', RI.rubid)) AND ecartattribut = 'OPTION_CSCP'),
                    (SELECT valecartval FROM APPLE_ECART_VALCOL_RUB WHERE (modeleid, versionid, projetid, rubid) IN (('MODELE_ID', 'VERSION_ID', 'PROJET_ID', RI.rubid)) AND (tablerubid, colonneid) IN (('APPLE_TABLE_RUBRIQUE_CALCUL', 'OPTION_CSCP'))),
                    (SELECT option_cscp FROM APPLE_TABLE_RUBRIQUE_CALCUL WHERE (modeleid, versionid, projetid, rubid) IN (('MODELE_ID_1', 'VERSION_ID', 'PROJET_ID', RI.rubid)))
                )
            )
    END AS option_cscp,
    PHASES.phase
    FROM RUBRIQUES_INFOS RI
    CROSS JOIN PHASES
)
-- SELECT * FROM RUBRIQUES_FULL;


,
RUBRIQUES_REGLES AS (
    SELECT RF.*,
    CASE
        WHEN EXISTS (SELECT 1 FROM PEAR_REGLE_RUBRIQUE WHERE (clientid, projetid, rubid, phase) IN ((RF.clientid, RF.projetid, RF.rubid, RF.phase))) THEN
            (SELECT regleid FROM PEAR_REGLE_RUBRIQUE WHERE (clientid, projetid, rubid, phase) IN ((RF.clientid, RF.projetid, RF.rubid, RF.phase)))
        WHEN EXISTS (SELECT 1 FROM PEAR_REGLE_RUBRIQUE WHERE (clientid, projetid, rubid, phase) IN ((RF.clientid, 'PROJET_ID', RF.rubid, RF.phase))) THEN
            (SELECT regleid FROM PEAR_REGLE_RUBRIQUE WHERE (clientid, projetid, rubid, phase) IN ((RF.clientid, 'PROJET_ID', RF.rubid, RF.phase)))
         WHEN EXISTS (SELECT 1 FROM APPLE_REGLE_RUBRIQUE WHERE (modeleid, versionid, projetid, rubid, phase) IN (('MODELE_ID', 'VERSION_ID', 'PROJET_ID', RF.rubid, RF.phase))) THEN
           (SELECT regleid FROM APPLE_REGLE_RUBRIQUE WHERE (modeleid, versionid, projetid, rubid, phase) IN (('MODELE_ID', 'VERSION_ID', 'PROJET_ID', RF.rubid, RF.phase)))
        WHEN EXISTS (SELECT 1 FROM APPLE_REGLE_RUBRIQUE WHERE (modeleid, versionid, projetid, rubid, phase) IN (('MODELE_ID_1', 'VERSION_ID', 'PROJET_ID', RF.rubid, RF.phase))) THEN
           (SELECT regleid FROM APPLE_REGLE_RUBRIQUE WHERE (modeleid, versionid, projetid, rubid, phase) IN (('MODELE_ID_1', 'VERSION_ID', 'PROJET_ID', RF.rubid, RF.phase)))
    END AS regleid
    FROM RUBRIQUES_FULL RF
    --ORDER BY clientid, projetid, rubid, phase
)
 --SELECT * FROM RUBRIQUES_REGLES;


,
RUBRIQUES_SOLUTIONS AS (
   SELECT R1.*,
   CASE
        WHEN SUBSTR(regleid, 1, 2) = 'Z8' THEN(
            COALESCE (
                (SELECT texte FROM (SELECT texte FROM PEAR_REGLE WHERE (clientid, projetid, produitid, regleid) IN ((R1.clientid, r1.projetid, 'Z2M', regleid)) ORDER BY date_effet) WHERE ROWNUM = 1),
                (SELECT texte FROM (SELECT texte FROM PEAR_REGLE WHERE (clientid, projetid, produitid, regleid) IN ((R1.clientid, 'PROJET_ID', 'Z2M', regleid)) ORDER BY date_effet) WHERE ROWNUM = 1)
            )

        )
        
        WHEN SUBSTR(regleid, 1, 2) = 'Z4' THEN(
             SELECT texte FROM (
                    SELECT texte FROM APPLE_REGLE WHERE (modeleid, versionid, projetid, produitid, regleid) IN (('MODELE_ID', 'VERSION_ID', 'PROJET_ID', 'Z2M', regleid)) ORDER BY date_effet
            ) WHERE ROWNUM = 1
        )
        
        WHEN SUBSTR(regleid, 1, 2) = 'Z2' THEN(
             SELECT texte FROM (
                    SELECT texte FROM APPLE_REGLE WHERE (modeleid, versionid, projetid, produitid, regleid) IN (('MODELE_ID_1', 'VERSION_ID', 'PROJET_ID', 'Z2M', regleid)) ORDER BY date_effet
            ) WHERE ROWNUM = 1
        )
    
      
    END AS texte
   FROM RUBRIQUES_REGLES  R1
   JOIN (
        SELECT DISTINCT rubid
        FROM RUBRIQUES_REGLES 
        WHERE regleid IN ('REGLE_ID_1', 'REGLE_ID_2', 'REGLE_ID_3', 'REGLE_ID_4', 'REGLE_ID_5')
    ) R2 ON R1.rubid = R2.rubid
)
SELECT * FROM RUBRIQUES_SOLUTIONS
WHERE regleid IS NOT NULL
;