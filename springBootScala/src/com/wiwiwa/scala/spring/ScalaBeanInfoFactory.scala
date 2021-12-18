package com.wiwiwa.scala.spring

import org.springframework.beans.BeanInfoFactory
import org.springframework.core.Ordered

import java.beans.{BeanDescriptor, PropertyDescriptor, SimpleBeanInfo}
import java.lang.reflect.Method

class ScalaBeanInfoFactory extends BeanInfoFactory with Ordered:
  override def getBeanInfo(beanClass: Class[_]) =
    val isScalaClass = beanClass.getResource(beanClass.getSimpleName+".tasty") != null
    if !isScalaClass then null
    else new ScalaBeanInfo(beanClass)
  override def getOrder = 100

class ScalaBeanInfo(beanClass:Class[_]) extends SimpleBeanInfo:
  override def getBeanDescriptor = new BeanDescriptor(beanClass)
  override def getPropertyDescriptors =
    val methods = beanClass.getMethods
    methods.filter{m=> m.getParameterCount==0 && m.getReturnType!=classOf[Unit] && m.getDeclaringClass!=classOf[Object] }
      .foldLeft(List[PropertyDescriptor]()){ (props,reader)=>
        val propName = reader.getName.toList match
          case 'g'::'e'::'t'::c::rest if c.isUpper => (c.toLower+:rest).mkString
          case v => v.mkString
        val writer =
          val propType = reader.getReturnType
          def isWriter(m:Method,name:String) = m.getName==name && { m.getParameterTypes match
            case Array(c: Class[_]) => c == propType
            case _ => false
          }
          methods.find(isWriter(_,"set"+propName.capitalize))
            .orElse{methods.find(isWriter(_,propName))}
            .orElse{methods.find(isWriter(_,propName+"_$eq"))}.orNull
        if writer==null then props
        else props :+ new PropertyDescriptor(propName, reader, writer)
      }.toArray

object ScalaBeanInfo:
  /** Copy properties values from `src` object to `target` object,
   *  using BeanInfo defined by this class */
  def copyProperties(src:Any, target:Any) =
    val srcPDs = new ScalaBeanInfo(src.getClass).getPropertyDescriptors
    new ScalaBeanInfo(target.getClass).getPropertyDescriptors.foreach{ wpd=>
      srcPDs.find(_.getName==wpd.getName).map{ srcPD=>
        val propValue = srcPD.getReadMethod.invoke(src)
        val setter = wpd.getWriteMethod
        if setter!=null then
          if propValue!=null then
            setter.invoke(target, propValue)
        else
            val clsName = target.getClass.getSimpleName
            throw IllegalAccessException(s"Property $clsName.${wpd.getName} is readonly")
      }
    }
