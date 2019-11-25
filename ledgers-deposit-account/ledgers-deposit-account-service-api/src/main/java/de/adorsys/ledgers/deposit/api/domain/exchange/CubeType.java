
package de.adorsys.ledgers.deposit.api.domain.exchange;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for CubeType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CubeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Cube" type="{http://www.ecb.int/vocabulary/2002-08-01/eurofxref}CubeType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="currency" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="rate" type="{http://www.w3.org/2001/XMLSchema}float" />
 *       &lt;attribute name="time" type="{http://www.w3.org/2001/XMLSchema}date" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CubeType", namespace = "http://www.ecb.int/vocabulary/2002-08-01/eurofxref", propOrder = {
        "content"
})
public class CubeType {

    @XmlElementRef(name = "Cube", namespace = "http://www.ecb.int/vocabulary/2002-08-01/eurofxref", type = JAXBElement.class, required = false)
    @XmlMixed
    protected List<CubeType> content;
    @XmlAttribute(name = "currency")
    protected String currency;
    @XmlAttribute(name = "rate")
    protected Float rate;
    @XmlAttribute(name = "time")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar time;

    /**
     * Gets the value of the content property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link CubeType }{@code >}
     * {@link String }
     */
    public List<CubeType> getContent() {
        if (content == null) {
            content = new ArrayList<CubeType>();
        }
        return this.content;
    }

    /**
     * Gets the value of the currency property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the value of the currency property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCurrency(String value) {
        this.currency = value;
    }

    /**
     * Gets the value of the rate property.
     *
     * @return possible object is
     * {@link Float }
     */
    public Float getRate() {
        return rate;
    }

    /**
     * Sets the value of the rate property.
     *
     * @param value allowed object is
     *              {@link Float }
     */
    public void setRate(Float value) {
        this.rate = value;
    }

    /**
     * Gets the value of the time property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setTime(XMLGregorianCalendar value) {
        this.time = value;
    }

}
