<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/">
      <html><body bgcolor="#FFFFFF" text="#000000">
         <xsl:apply-templates/>
      </body></html>
    </xsl:template>

<xsl:template match="integration-plan">

  <table width="100%" height="0%" border="0" bgcolor="#FAF0E6" cellpadding="0" cellspacing="0">
      <tr>
         <th valign="top" bgcolor="#C0C0C0">
            <xsl:value-of select="description"/>
         </th>
      </tr>
      <tr>
         <td style="height:5px" bgcolor="#FFFFFF"/>
      </tr>
      <tr>
         <td>
           <table width="100%" height="0%" border="0" bgcolor="#FAF0E6" cellpadding="1" cellspacing="2">
              <tr>
                <th colspan="5" align="left" valign="top" bgcolor="#778899">
                  Déroulement du contrôle :
                 </th>
              </tr>
              <xsl:for-each select="plans/plan/step">
              <xsl:sort select="@priority" data-type="number"/>
              <tr>
                 <td>
                   <xsl:value-of select="@priority"/>
                 </td>
                 <td>
                   <b><xsl:value-of select="@type"/></b>
                   <xsl:if test="@step_for"> ( <xsl:value-of select="@step_for"/> ) </xsl:if>
                 </td>
                 <td>
                   <b><xsl:value-of select="@error_code"/></b>
                 </td>
                 <td>
                  <a href="#{@id}">  <xsl:value-of select="@id"/></a>
                 </td>
                 <td>
                  <xsl:value-of select="description"/>
                 </td>
              </tr>
              </xsl:for-each>
           </table>
         </td>
      </tr>
      <!-- Plan delete -->
      <tr>
         <td>
           <table width="100%" height="0%" border="0" bgcolor="#FAF0E6" cellpadding="1" cellspacing="2">
              <tr>
                <th colspan="5" align="left" valign="top" bgcolor="#778899">
                  Déroulement des contrôles lors d'un delete :
                 </th>
              </tr>
              <xsl:for-each select="plans_delete/plan/step">
              <xsl:sort select="@priority" data-type="number"/>
              <tr>
                 <td>
                   <b><xsl:value-of select="@error_code"/></b>
                 </td>
                 <td>
                   <b><xsl:value-of select="@type"/></b>
                   <xsl:if test="@step_for"> ( <xsl:value-of select="@step_for"/> ) </xsl:if>
                 </td>
                 <td>

                  <a href="#{@id}">  <xsl:value-of select="@id"/></a>
                 </td>
                 <td>
                  <xsl:value-of select="description"/>
                 </td>
              </tr>
              </xsl:for-each>
           </table>
         </td>
      </tr>
         <!-- Dispatch :   -->
      <tr>
         <td>
           <table width="100%" height="0%" border="0" bgcolor="#FAF0E6" cellpadding="1" cellspacing="2">
              <tr>
                <th colspan="4" align="left" valign="top" bgcolor="#778899">
                  Déroulement de la phase final d'intégration :
                 </th>
              </tr>
              <xsl:for-each select="dispatch/step">
              <xsl:sort select="@priority" data-type="number"/>
              <tr>
                 <td>
                   <b><xsl:value-of select="@type"/></b>
                 </td>
                 <td>

                  <a href="#{@id}">  <xsl:value-of select="@id"/></a>
                 </td>
                 <td>
                  <xsl:value-of select="description"/>
                 </td>
              </tr>
              </xsl:for-each>
           </table>
         </td>
      </tr>
         <!-- END Dispatch -->
      <tr>
         <td style="height:15px" bgcolor="#FFFFFF"/>
      </tr>
      <tr>
         <td>
           <xsl:apply-templates select="dictionary"/>
         </td>
      </tr>
       <tr>
         <td style="height:5px" bgcolor="#FFFFFF"/>
      </tr>
      <tr>
         <td>
            <xsl:apply-templates select="table"/>
          </td>
      </tr>
      <tr>
        <td style="height:5px" bgcolor="#FFFFFF"/>
      </tr>
      <tr>
        <td>
          <xsl:apply-templates select="shipment"/>
        </td>
      </tr>
      <tr>
        <td style="height:5px" bgcolor="#FFFFFF"/>
      </tr>
      <tr>
        <td>
          <xsl:apply-templates select="plans/plan"/>
        </td>
      </tr>
      <tr>
        <td>
          <xsl:apply-templates select="dispatch"/>
        </td>
      </tr>

  </table>
</xsl:template>

<xsl:template match="dictionary">
  <table width="100%" height="0%" border="0" bgcolor="#FAF0E6" cellpadding="0" cellspacing="0">
    <tr>
      <th colspan="3" align="left" valign="top" bgcolor="#778899">
      Dictionnaire :
      </th>
    </tr>
    <xsl:apply-templates select="variable"/>
  </table>
</xsl:template>

<xsl:template match="variable">
   <tr>
     <td style="width:30px"> &#160; </td>
     <td align="left"><i><xsl:value-of select="@name"/></i></td>
     <td align="left"><i><xsl:value-of select="@value"/></i></td>
   </tr>
</xsl:template>


<xsl:template match="table">
 <table width="100%" height="0%" border="0" bgcolor="#FAF0E6" cellpadding="0" cellspacing="0">
    <tr>
      <th colspan="2" align="left" valign="top" bgcolor="#778899">
         Table :
      </th>
    </tr>
    <tr>
      <td style="width:130px">&#160; </td>
      <td valign="top" >
      <pre><xsl:apply-templates/></pre>
      </td>
   </tr>
 </table>
</xsl:template>


<xsl:template match="shipment">
 <table width="100%" height="0%" border="0" bgcolor="#FAF0E6" cellpadding="0" cellspacing="0">
    <tr>
      <th colspan="4" align="left" valign="top" bgcolor="#778899">
         Typage :
      </th>
    </tr>
    <tr>
      <td  style="width:30px" align="right">De :</td>
      <td align="left"><i><xsl:value-of select="@from"/></i></td>
      <td  style="width:30px" align="right">vers : </td>
      <td align="left"><i><xsl:value-of select="@to"/></i></td>
   </tr>
   <tr>
      <td  style="width:30px" align="right">Clé :</td>
      <td align="left"><i><xsl:value-of select="from-pk-field"/></i></td>
      <td  style="width:30px" align="right">Clause where </td>
      <td align="left"><i><xsl:value-of select="select-where-clause"/></i></td>
    </tr>
 </table>
</xsl:template>

<xsl:template match="plan">
 <table width="100%" height="0%" border="0" bgcolor="#FAF0E6" cellpadding="0" cellspacing="0">
    <tr>
      <th  colspan="2" align="left" valign="top" bgcolor="#778899">
         Déroulement:
      </th>
    </tr>
    <tr>
      <td bgcolor="#778899" style="width:5px" >
      </td>
      <td>
         <xsl:apply-templates select="step">
              <xsl:sort select="@priority" data-type="number"/>
         </xsl:apply-templates>
      </td>
    </tr>
 </table>
</xsl:template>

<xsl:template match="dispatch">
 <table width="100%" height="0%" border="0" bgcolor="#FAF0E6" cellpadding="0" cellspacing="0">
    <tr>
      <th  colspan="2" align="left" valign="top" bgcolor="#778899">
         Déroulement des tests :
      </th>
    </tr>
    <tr>
      <td bgcolor="#778899" style="width:5px" >
      </td>
      <td>
         <xsl:apply-templates select="step">
              <xsl:sort select="@priority" data-type="number"/>
         </xsl:apply-templates>
      </td>
    </tr>
 </table>
</xsl:template>

<xsl:template match="step">
<table width="100%" height="0%" border="1"  bgcolor="#FAF0E6" cellpadding="0" cellspacing="0">
  <tr>
    <th style="width:10px" valign="top" bgcolor="#B0C4DE">
      <xsl:value-of select="@priority"/><br/>

    </th>
    <td>
      <table width="100%"  height="0%" border="0" bgcolor="#FAF0E6" cellpadding="2" cellspacing="0">
        <tr bgcolor="#B0C4DE">
          <th colspan="3"> <xsl:value-of select="@id"/>
          <A NAME="{@id}"></A>
          </th>
        </tr>
         <tr>
          <td colspan="3" bgcolor="#DFDFFF"><b>Step père : </b>&#160;<b><i><xsl:value-of select="@inheritId"/></i></b></td>
        </tr>
         <tr>
          <td colspan="3" bgcolor="#DFDFFF"><b>Type : </b>&#160;<b><i><xsl:value-of select="@type"/></i></b></td>
        </tr>
         <tr>
          <td colspan="3" bgcolor="#DFDFFF"><b>Code Erreur : </b>&#160;<b><i><xsl:value-of select="@error_code"/></i></b></td>
        </tr>
         <tr>
          <td colspan="3" bgcolor="#DFDFFF"><b>Message Erreur : </b>&#160;<b><i><xsl:value-of select="@error_message"/></i></b></td>
        </tr>
        <tr>
          <td colspan="3" bgcolor="#DFDFFF"><b>Description</b></td>
        </tr>
        <tr>
          <td colspan="3" ><i><xsl:value-of select="description"/></i></td>
        </tr>
        <xsl:apply-templates select="dictionary"/>
        <tr>
          <td colspan="3" bgcolor="#DFDFFF"><b>Paramètres</b></td>
        </tr>
        <xsl:apply-templates select="parameter"/>
        <tr>
          <td colspan="3" bgcolor="#DFDFFF"><b>Requètes</b></td>
        </tr>
        <tr>
          <td colspan="3">
            <pre><xsl:value-of select="query"/></pre>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
  <td colspan="2" style="width:10px" bgcolor="#FFFFFF"> &#160; </td>
</tr>
</table>
</xsl:template>

<xsl:template match="parameter">
<tr>
  <td   style="width:10px" ><i><xsl:value-of select="@index"/></i></td>
  <td ><i><xsl:value-of select="@type"/></i></td>
  <td ><i><xsl:value-of select="."/></i></td>
</tr>
</xsl:template>

</xsl:stylesheet>
